package hr.prism.board.service;

import com.google.common.base.Joiner;
import hr.prism.board.domain.*;
import hr.prism.board.dto.DocumentDTO;
import hr.prism.board.dto.ResourceEventDTO;
import hr.prism.board.enums.Gender;
import hr.prism.board.enums.MemberCategory;
import hr.prism.board.enums.Role;
import hr.prism.board.enums.Scope;
import hr.prism.board.event.ActivityEvent;
import hr.prism.board.event.EventProducer;
import hr.prism.board.event.NotificationEvent;
import hr.prism.board.exception.BoardDuplicateException;
import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.BoardForbiddenException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.repository.ResourceEventRepository;
import hr.prism.board.repository.ResourceEventSearchRepository;
import hr.prism.board.utils.BoardUtils;
import hr.prism.board.value.ResourceEventSummary;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static hr.prism.board.enums.Activity.RESPOND_POST_ACTIVITY;
import static hr.prism.board.enums.Notification.RESPOND_POST_NOTIFICATION;
import static java.util.Collections.singletonList;

@Service
@Transactional
@SuppressWarnings({"SpringAutowiredFieldsWarningInspection", "WeakerAccess", "UnusedReturnValue"})
public class ResourceEventService {

    @Inject
    private ResourceEventRepository resourceEventRepository;

    @Inject
    private ResourceEventSearchRepository resourceEventSearchRepository;

    @Inject
    private DocumentService documentService;

    @Inject
    private UserService userService;

    @Inject
    private UserRoleService userRoleService;

    @Inject
    private EntityManager entityManager;

    @Inject
    private EventProducer eventProducer;

    public ResourceEvent findOne(Long resourceEventId) {
        return resourceEventRepository.findOne(resourceEventId);
    }

    public ResourceEvent createPostView(Post post, User user, String ipAddress) {
        if (user == null && ipAddress == null) {
            throw new BoardException(ExceptionCode.UNIDENTIFIABLE_RESOURCE_EVENT, "No way to identify post viewer");
        }

        ResourceEvent resourceEvent = new ResourceEvent().setResource(post).setEvent(hr.prism.board.enums.ResourceEvent.VIEW);
        if (user == null) {
            resourceEvent.setIpAddress(ipAddress);
            resourceEvent.setCreatorId(post.getCreatorId());
        } else {
            resourceEvent.setUser(user);
        }

        return saveResourceEvent(post, resourceEvent);
    }

    public ResourceEvent createPostReferral(Post post, User user) {
        String referral = DigestUtils.sha256Hex(UUID.randomUUID().toString());
        return saveResourceEvent(post, new ResourceEvent().setResource(post).setEvent(hr.prism.board.enums.ResourceEvent.REFERRAL).setUser(user).setReferral(referral));
    }

    public ResourceEvent getOrCreatePostResponse(Post post, User user, ResourceEventDTO resourceEventDTO) {
        if (post.getApplyEmail() == null) {
            throw new BoardException(ExceptionCode.INVALID_RESOURCE_EVENT, "Post no longer accepting applications");
        }

        DocumentDTO documentResumeDTO = resourceEventDTO.getDocumentResume();
        String websiteResume = resourceEventDTO.getWebsiteResume();
        String coveringNote = resourceEventDTO.getCoveringNote();

        ResourceEvent previousResponse = findByResourceAndEventAndUser(post, hr.prism.board.enums.ResourceEvent.RESPONSE, user);
        if (previousResponse != null) {
            throw new BoardDuplicateException(ExceptionCode.DUPLICATE_RESOURCE_EVENT, "User already responded", previousResponse.getId());
        }

        Document documentResume = documentService.getOrCreateDocument(documentResumeDTO);
        UserRole userRole = userRoleService.findByResourceAndUserAndRole(post.getParent().getParent(), user, Role.MEMBER);
        ResourceEvent response = saveResourceEvent(post, new ResourceEvent().setResource(post).setEvent(hr.prism.board.enums.ResourceEvent.RESPONSE)
            .setUser(user).setGender(user.getGender()).setAgeRange(user.getAgeRange()).setLocationNationality(user.getLocationNationality())
            .setMemberCategory(userRole.getMemberCategory()).setMemberProgram(userRole.getMemberProgram()).setMemberYear(userRole.getMemberYear())
            .setDocumentResume(documentResume).setWebsiteResume(websiteResume).setCoveringNote(coveringNote));
        setIndexData(response);
        if (BooleanUtils.isTrue(resourceEventDTO.getDefaultResume())) {
            userService.updateUserResume(user, documentResume, websiteResume);
        }

        Long postId = post.getId();
        Long responseId = response.getId();

        eventProducer.produce(
            new ActivityEvent(this, postId, ResourceEvent.class, responseId,
                singletonList(
                    new hr.prism.board.workflow.Activity()
                        .setScope(Scope.POST)
                        .setRole(Role.ADMINISTRATOR)
                        .setActivity(RESPOND_POST_ACTIVITY))),
            new NotificationEvent(this, postId, responseId,
                singletonList(
                    new hr.prism.board.workflow.Notification()
                        .setNotification(RESPOND_POST_NOTIFICATION)
                        .addAttachment(
                            new hr.prism.board.workflow.Notification.Attachment()
                                .setName(documentResume.getFileName())
                                .setUrl(documentResume.getCloudinaryUrl())
                                .setLabel("Application")))));

        return response;
    }

    public ResourceEvent findByResourceAndEventAndUser(Resource resource, hr.prism.board.enums.ResourceEvent event, User user) {
        List<Long> ids = resourceEventRepository.findMaxIdsByResourcesAndEventAndUser(singletonList(resource), event, user);
        return ids.isEmpty() ? null : resourceEventRepository.findOne(ids.get(0));
    }

    public <T extends Resource> List<ResourceEvent> findByResourceIdsAndEventAndUser(List<T> resources, hr.prism.board.enums.ResourceEvent event, User user) {
        List<Long> ids = resourceEventRepository.findMaxIdsByResourcesAndEventAndUser(resources, event, user);
        return ids.isEmpty() ? Collections.emptyList() : resourceEventRepository.findOnes(ids);
    }

    public ResourceEvent getAndConsumeReferral(String referral) {
        ResourceEvent resourceEvent = resourceEventRepository.findByReferral(referral);
        if (resourceEvent == null) {
            // Bad referral, or referral consumed already - client should request a new one
            throw new BoardForbiddenException(ExceptionCode.FORBIDDEN_REFERRAL, "Referral does not exist or has been consumed");
        }

        User user = resourceEvent.getUser();
        resourceEvent.setGender(user.getGender());
        resourceEvent.setAgeRange(user.getAgeRange());
        resourceEvent.setLocationNationality(user.getLocationNationality());

        UserRole userRole = userRoleService.findByResourceAndUserAndRole(resourceEvent.getResource().getParent().getParent(), user, Role.MEMBER);
        resourceEvent.setMemberCategory(userRole.getMemberCategory());
        resourceEvent.setMemberProgram(userRole.getMemberProgram());
        resourceEvent.setMemberYear(userRole.getMemberYear());

        setIndexData(resourceEvent);
        resourceEvent.setReferral(null);
        resourceEvent = resourceEventRepository.update(resourceEvent);
        updateResourceEventSummary((Post) resourceEvent.getResource());
        return resourceEvent;
    }

    public List<ResourceEvent> findByIpAddresses(Collection<String> ipAddresses) {
        return resourceEventRepository.findByEventAndIpAddresses(hr.prism.board.enums.ResourceEvent.VIEW, ipAddresses);
    }

    public void createSearchResults(String search, String searchTerm, Collection<Long> userIds) {
        resourceEventSearchRepository.insertBySearch(search, LocalDateTime.now(), BoardUtils.makeSoundex(searchTerm), userIds);
    }

    public void deleteSearchResults(String search) {
        resourceEventSearchRepository.deleteBySearch(search);
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

        resourceEvent.setIndexData(Joiner.on(" ").skipNulls().join(BoardUtils.makeSoundex(genderString,
            locationNationalityString, memberCategoryString, resourceEvent.getMemberProgram()), resourceEvent.getMemberYear()));
    }

    private void updateResourceEventSummary(Post post) {
        entityManager.flush();
        Map<hr.prism.board.enums.ResourceEvent, ResourceEventSummary> summaries = resourceEventRepository.findUserSummaryByResource(post)
            .stream().collect(Collectors.toMap(ResourceEventSummary::getKey, resourceEventSummary -> resourceEventSummary));
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
            switch (summary.getKey()) {
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
            }
        }
    }

}
