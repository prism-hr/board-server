package hr.prism.board.service;

import com.google.common.base.Joiner;
import hr.prism.board.domain.*;
import hr.prism.board.dto.DocumentDTO;
import hr.prism.board.dto.ResourceEventDTO;
import hr.prism.board.enums.Gender;
import hr.prism.board.enums.MemberCategory;
import hr.prism.board.event.ActivityEvent;
import hr.prism.board.event.EventProducer;
import hr.prism.board.event.NotificationEvent;
import hr.prism.board.exception.BoardDuplicateException;
import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.BoardForbiddenException;
import hr.prism.board.repository.ResourceEventRepository;
import hr.prism.board.value.ResourceEventSummary;
import hr.prism.board.workflow.Notification;
import hr.prism.board.workflow.Notification.Attachment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;

import static hr.prism.board.enums.Activity.RESPOND_POST_ACTIVITY;
import static hr.prism.board.enums.Notification.RESPOND_POST_NOTIFICATION;
import static hr.prism.board.enums.ResourceEvent.*;
import static hr.prism.board.enums.Role.ADMINISTRATOR;
import static hr.prism.board.enums.Role.MEMBER;
import static hr.prism.board.enums.Scope.POST;
import static hr.prism.board.exception.ExceptionCode.*;
import static hr.prism.board.utils.BoardUtils.makeSoundex;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.codec.digest.DigestUtils.sha256Hex;
import static org.apache.commons.lang3.BooleanUtils.isTrue;

@Service
@Transactional
public class ResourceEventService {

    private final ResourceEventRepository resourceEventRepository;

    private final DocumentService documentService;

    private final UserService userService;

    private final UserRoleService userRoleService;

    private final EntityManager entityManager;

    private final EventProducer eventProducer;

    @Inject
    public ResourceEventService(ResourceEventRepository resourceEventRepository, DocumentService documentService,
                                UserService userService, UserRoleService userRoleService, EntityManager entityManager,
                                EventProducer eventProducer) {
        this.resourceEventRepository = resourceEventRepository;
        this.documentService = documentService;
        this.userService = userService;
        this.userRoleService = userRoleService;
        this.entityManager = entityManager;
        this.eventProducer = eventProducer;
    }

    public ResourceEvent getById(Long resourceEventId) {
        return resourceEventRepository.findOne(resourceEventId);
    }

    @SuppressWarnings("UnusedReturnValue")
    public ResourceEvent createPostView(Post post, User user, String ipAddress) {
        if (user == null && ipAddress == null) {
            throw new BoardException(UNIDENTIFIABLE_RESOURCE_EVENT, "No way to identify post viewer");
        }

        ResourceEvent resourceEvent =
            new ResourceEvent()
                .setResource(post)
                .setEvent(VIEW);

        if (user == null) {
            resourceEvent.setIpAddress(ipAddress);
            resourceEvent.setCreatorId(post.getCreatorId());
        } else {
            resourceEvent.setUser(user);
        }

        return saveResourceEvent(post, resourceEvent);
    }

    @SuppressWarnings("UnusedReturnValue")
    public ResourceEvent createPostReferral(Post post, User user) {
        String referral = sha256Hex(randomUUID().toString());
        return saveResourceEvent(post,
            new ResourceEvent()
                .setResource(post)
                .setEvent(REFERRAL)
                .setUser(user)
                .setReferral(referral));
    }

    public ResourceEvent getOrCreatePostResponse(Post post, User user, ResourceEventDTO resourceEventDTO) {
        if (post.getApplyEmail() == null) {
            throw new BoardException(INVALID_RESOURCE_EVENT, "Post no longer accepting applications");
        }

        DocumentDTO documentResumeDTO = resourceEventDTO.getDocumentResume();
        String websiteResume = resourceEventDTO.getWebsiteResume();
        String coveringNote = resourceEventDTO.getCoveringNote();

        ResourceEvent previousResponse = findByResourceAndEventAndUser(post, RESPONSE, user);
        if (previousResponse != null) {
            throw new BoardDuplicateException(
                DUPLICATE_RESOURCE_EVENT, "User already responded", previousResponse.getId());
        }

        Document documentResume = documentService.getOrCreateDocument(documentResumeDTO);
        UserRole userRole = userRoleService.getByResourceUserAndRole(post.getParent().getParent(), user, MEMBER);

        ResourceEvent response = saveResourceEvent(post,
            new ResourceEvent()
                .setResource(post)
                .setEvent(RESPONSE)
                .setUser(user)
                .setGender(user.getGender())
                .setAgeRange(user.getAgeRange())
                .setLocationNationality(user.getLocationNationality())
                .setMemberCategory(userRole.getMemberCategory())
                .setMemberProgram(userRole.getMemberProgram())
                .setMemberYear(userRole.getMemberYear())
                .setDocumentResume(documentResume)
                .setWebsiteResume(websiteResume)
                .setCoveringNote(coveringNote));

        setIndexData(response);
        if (isTrue(resourceEventDTO.getDefaultResume())) {
            userService.updateUserResume(user, documentResume, websiteResume);
        }

        Long postId = post.getId();
        Long responseId = response.getId();

        eventProducer.produce(
            new ActivityEvent(this, postId, ResourceEvent.class, responseId,
                singletonList(
                    new hr.prism.board.workflow.Activity()
                        .setScope(POST)
                        .setRole(ADMINISTRATOR)
                        .setActivity(RESPOND_POST_ACTIVITY))),
            new NotificationEvent(this, postId, responseId,
                singletonList(
                    new Notification()
                        .setNotification(RESPOND_POST_NOTIFICATION)
                        .addAttachment(
                            new Attachment()
                                .setName(documentResume.getFileName())
                                .setUrl(documentResume.getCloudinaryUrl())
                                .setLabel("Application")))));

        return response;
    }

    public ResourceEvent findByResourceAndEventAndUser(Resource resource, hr.prism.board.enums.ResourceEvent event,
                                                       User user) {
        List<Long> ids =
            resourceEventRepository.findMaxIdsByResourcesAndEventAndUser(singletonList(resource), event, user);
        return ids.isEmpty() ? null : resourceEventRepository.findOne(ids.get(0));
    }

    public <T extends Resource> List<ResourceEvent> findByResourceIdsAndEventAndUser(
        List<T> resources, hr.prism.board.enums.ResourceEvent event, User user) {
        List<Long> ids = resourceEventRepository.findMaxIdsByResourcesAndEventAndUser(resources, event, user);
        return ids.isEmpty() ? emptyList() : resourceEventRepository.findOnes(ids);
    }

    public ResourceEvent getAndConsumeReferral(String referral) {
        ResourceEvent resourceEvent = resourceEventRepository.findByReferral(referral);
        if (resourceEvent == null) {
            // Bad referral, or referral consumed already - client should request a new one
            throw new BoardForbiddenException(FORBIDDEN_REFERRAL, "Referral does not exist or has been consumed");
        }

        User user = resourceEvent.getUser();
        resourceEvent.setGender(user.getGender());
        resourceEvent.setAgeRange(user.getAgeRange());
        resourceEvent.setLocationNationality(user.getLocationNationality());

        Department department = (Department) resourceEvent.getResource().getParent().getParent();
        UserRole userRole = userRoleService.getByResourceUserAndRole(department, user, MEMBER);

        resourceEvent.setMemberCategory(userRole.getMemberCategory());
        resourceEvent.setMemberProgram(userRole.getMemberProgram());
        resourceEvent.setMemberYear(userRole.getMemberYear());

        setIndexData(resourceEvent);
        resourceEvent.setReferral(null);

        updateResourceEventSummary((Post) resourceEvent.getResource());
        return resourceEvent;
    }

    private ResourceEvent saveResourceEvent(Post post, ResourceEvent resourceEvent) {
        resourceEvent = resourceEventRepository.save(resourceEvent);
        updateResourceEventSummary(post);
        return resourceEvent;
    }

    public void setIndexData(ResourceEvent resourceEvent) {
        String memberCategoryString = null;
        MemberCategory memberCategory = resourceEvent.getMemberCategory();
        if (memberCategory != null) {
            memberCategoryString = memberCategory.name();
        }

        String genderString = null;
        Gender gender = resourceEvent.getGender();
        if (gender != null) {
            genderString = gender.name();
        }

        String locationNationalityString = null;
        Location locationNationality = resourceEvent.getLocationNationality();
        if (locationNationality != null) {
            locationNationalityString = locationNationality.getName();
        }

        String soundex = makeSoundex(genderString,
            locationNationalityString, memberCategoryString, resourceEvent.getMemberProgram());
        requireNonNull(soundex, "soundex cannot be null");
        resourceEvent.setIndexData(Joiner.on(" ").skipNulls().join(soundex, resourceEvent.getMemberYear()));
    }

    private void updateResourceEventSummary(Post post) {
        entityManager.flush();
        Map<hr.prism.board.enums.ResourceEvent, ResourceEventSummary> summaries =
            resourceEventRepository.findUserSummaryByResource(post).stream()
                .collect(toMap(ResourceEventSummary::getKey, resourceEventSummary -> resourceEventSummary));

        resourceEventRepository.findIpAddressSummaryByResource(post).forEach(summary -> {
            hr.prism.board.enums.ResourceEvent key = summary.getKey();
            ResourceEventSummary value = summaries.get(summary.getKey());
            if (value == null) {
                summaries.put(key, summary);
            } else {
                value.merge(summary);
            }
        });

        for (ResourceEventSummary summary : summaries.values()) {
            hr.prism.board.enums.ResourceEvent summaryKey = summary.getKey();
            switch (summaryKey) {
                case VIEW:
                    post.setViewCount(summary.getCount());
                    post.setLastViewTimestamp(summary.getLastTimestamp());
                    break;
                case REFERRAL:
                    post.setReferralCount(summary.getCount());
                    post.setLastReferralTimestamp(summary.getLastTimestamp());
                    break;
                case RESPONSE:
                    post.setResponseCount(summary.getCount());
                    post.setLastResponseTimestamp(summary.getLastTimestamp());
                    break;
                default:
                    throw new IllegalStateException("Unexpected event type: " + summaryKey);
            }
        }
    }

}
