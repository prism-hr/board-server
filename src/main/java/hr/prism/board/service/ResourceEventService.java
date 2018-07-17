package hr.prism.board.service;

import hr.prism.board.dao.ResourceEventDAO;
import hr.prism.board.domain.*;
import hr.prism.board.dto.DocumentDTO;
import hr.prism.board.dto.ResourceEventDTO;
import hr.prism.board.enums.Role;
import hr.prism.board.event.ActivityEvent;
import hr.prism.board.event.EventProducer;
import hr.prism.board.event.NotificationEvent;
import hr.prism.board.exception.BoardDuplicateException;
import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.BoardForbiddenException;
import hr.prism.board.repository.ResourceEventRepository;
import hr.prism.board.value.DemographicDataStatus;
import hr.prism.board.value.ResourceEventSummary;
import hr.prism.board.workflow.Notification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static hr.prism.board.enums.Action.EDIT;
import static hr.prism.board.enums.Action.PURSUE;
import static hr.prism.board.enums.Activity.RESPOND_POST_ACTIVITY;
import static hr.prism.board.enums.Notification.RESPOND_POST_NOTIFICATION;
import static hr.prism.board.enums.ResourceEvent.*;
import static hr.prism.board.enums.Role.ADMINISTRATOR;
import static hr.prism.board.enums.Role.MEMBER;
import static hr.prism.board.enums.Scope.POST;
import static hr.prism.board.exception.ExceptionCode.*;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;
import static java.util.UUID.randomUUID;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.codec.digest.DigestUtils.sha256Hex;
import static org.apache.commons.lang3.BooleanUtils.isTrue;
import static org.springframework.transaction.annotation.Isolation.SERIALIZABLE;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

@Service
@Transactional
public class ResourceEventService {

    private final ResourceEventRepository resourceEventRepository;

    private final ResourceEventDAO resourceEventDAO;

    private final ActionService actionService;

    private final DocumentService documentService;

    private final UserService userService;

    private final ResourceService resourceService;

    private final ActivityService activityService;

    private final DepartmentUserService departmentUserService;

    private final EventProducer eventProducer;

    private final EntityManager entityManager;

    @Inject
    public ResourceEventService(ResourceEventRepository resourceEventRepository, ResourceEventDAO resourceEventDAO,
                                ActionService actionService, DocumentService documentService, UserService userService,
                                ResourceService resourceService, ActivityService activityService,
                                DepartmentUserService departmentUserService, EventProducer eventProducer,
                                EntityManager entityManager) {
        this.resourceEventRepository = resourceEventRepository;
        this.resourceEventDAO = resourceEventDAO;
        this.actionService = actionService;
        this.documentService = documentService;
        this.userService = userService;
        this.resourceService = resourceService;
        this.activityService = activityService;
        this.departmentUserService = departmentUserService;
        this.eventProducer = eventProducer;
        this.entityManager = entityManager;
    }

    public ResourceEvent getById(Long resourceEventId) {
        return resourceEventRepository.findOne(resourceEventId);
    }

    @Transactional(propagation = REQUIRES_NEW, isolation = SERIALIZABLE)
    public Post processView(Long postId, User user, String ipAddress, boolean recordView) {
        Post post = (Post) resourceService.getResource(user, POST, postId);
        if (!recordView) return post;

        if (user == null && ipAddress == null) {
            throw new BoardException(UNIDENTIFIABLE_RESOURCE_EVENT, "No way to identify post viewer");
        }

        ResourceEvent resourceEvent =
            new ResourceEvent()
                .setResource(post)
                .setUser(user)
                .setIpAddress(ipAddress)
                .setEvent(VIEW);

        Role role = null;
        if (user == null) {
            resourceEvent.setCreatorId(post.getCreatorId());
            resourceEventRepository.save(resourceEvent);
        } else {
            DemographicDataStatus demographicDataStatus = getDemographicDataStatus(user, post);
            post.setDemographicDataStatus(demographicDataStatus);

            resourceEvent.setRole(demographicDataStatus.getRole());
            resourceEventRepository.save(resourceEvent);

            if (actionService.canExecuteAction(post, PURSUE)) {
                role = demographicDataStatus.getRole();

                post.setDemographicDataStatus(demographicDataStatus);
                if (demographicDataStatus.isReady() && post.getApplyEmail() == null) {
                    createPostReferral(post, user);
                }
            }

            entityManager.flush();
            post.setReferral(getResourceEvent(post, REFERRAL, user));
            post.setResponse(getResourceEvent(post, RESPONSE, user));
        }

        updateResourceEventSummaries(post, role);
        return post;
    }

    @Transactional(propagation = REQUIRES_NEW, isolation = SERIALIZABLE)
    public String processReferral(Long postId, User user, String referral) {
        Post post = (Post) resourceService.getResource(user, POST, postId);
        actionService.executeAction(user, post, PURSUE, () -> post);

        ResourceEvent resourceEvent = resourceEventRepository.findByResourceAndUserAndReferral(post, user, referral);
        if (resourceEvent == null) {
            throw new BoardForbiddenException(FORBIDDEN_REFERRAL,
                "User: " + user + " cannot redeem referral: " + referral + " for post: " + post);
        }

        Document applyDocument = post.getApplyDocument();
        String redirect = applyDocument == null ? post.getApplyWebsite() : applyDocument.getCloudinaryUrl();
        if (redirect == null) {
            throw new BoardException(INVALID_REFERRAL, "Post: " + post + " no longer accepting referrals");
        }

        DemographicDataStatus demographicDataStatus = getDemographicDataStatus(user, post);
        departmentUserService.checkValidDemographicData(demographicDataStatus);

        resourceEvent.setReferral(null);
        Role role = demographicDataStatus.getRole();
        if (role == MEMBER) {
            resourceEvent.setGender(user.getGender());
            resourceEvent.setAgeRange(user.getAgeRange());
            resourceEvent.setLocationNationality(user.getLocationNationality());

            resourceEvent.setMemberCategory(demographicDataStatus.getMemberCategory());
            resourceEvent.setMemberProgram(demographicDataStatus.getMemberProgram());
            resourceEvent.setMemberYear(demographicDataStatus.getMemberYear());

            resourceEvent.setIndexData();
            updateResourceEventSummaries(post, role);
        }

        return redirect;
    }

    @Transactional(propagation = REQUIRES_NEW, isolation = SERIALIZABLE)
    public ResourceEvent processResponse(Long postId, User user, ResourceEventDTO resourceEventDTO) {
        Post post = (Post) resourceService.getResource(user, POST, postId);
        actionService.executeAction(user, post, PURSUE, () -> post);

        if (post.getApplyEmail() == null) {
            throw new BoardException(INVALID_RESPONSE, "Post: " + post + " no longer accepting responses");
        }

        ResourceEvent previousResponse = getResourceEvent(post, RESPONSE, user);
        if (previousResponse != null) {
            throw new BoardDuplicateException(
                DUPLICATE_RESPONSE, "User already responded to post: " + post, previousResponse.getId());
        }

        DemographicDataStatus demographicDataStatus = getDemographicDataStatus(user, post);
        departmentUserService.checkValidDemographicData(demographicDataStatus);

        DocumentDTO documentResumeDTO = resourceEventDTO.getDocumentResume();
        Document documentResume = documentService.getOrCreateDocument(documentResumeDTO);
        String websiteResume = resourceEventDTO.getWebsiteResume();
        String coveringNote = resourceEventDTO.getCoveringNote();

        ResourceEvent resourceEvent =
            resourceEventRepository.save(
                new ResourceEvent()
                    .setResource(post)
                    .setEvent(RESPONSE)
                    .setUser(user)
                    .setDocumentResume(documentResume)
                    .setWebsiteResume(websiteResume)
                    .setCoveringNote(coveringNote));

        Role role = demographicDataStatus.getRole();
        resourceEvent.setRole(role);

        if (role == MEMBER) {
            resourceEvent
                .setGender(user.getGender())
                .setAgeRange(user.getAgeRange())
                .setLocationNationality(user.getLocationNationality())
                .setMemberCategory(demographicDataStatus.getMemberCategory())
                .setMemberProgram(demographicDataStatus.getMemberProgram())
                .setMemberYear(demographicDataStatus.getMemberYear())
                .setIndexData();

            updateResourceEventSummaries(post, role);
            sendResponseNotifications(resourceEvent);
        }

        if (isTrue(resourceEventDTO.getDefaultResume())) {
            userService.updateUserResume(user, documentResume, websiteResume);
        }

        resourceEvent.setExposeResponseData(true);
        return resourceEvent;
    }

    public ResourceEvent getResponse(User user, Long resourceId, Long resourceEventId) {
        Post post = (Post) resourceService.getResource(user, POST, resourceId);
        actionService.executeAction(user, post, EDIT, () -> post);
        ResourceEvent resourceEvent = getById(resourceEventId);
        resourceEvent.setExposeResponseData(resourceEvent.getUser().equals(user));
        return resourceEvent;
    }

    public List<ResourceEvent> getResponses(User user, Long resourceId, String searchTerm) {
        Post post = (Post) resourceService.getResource(user, POST, resourceId);
        actionService.executeAction(user, post, EDIT, () -> post);
        return resourceEventDAO.getResponses(user, post, searchTerm);
    }

    public ResourceEvent createResponseView(User user, Long postId, Long responseId) {
        ResourceEvent resourceEvent = getResponse(user, postId, responseId);
        activityService.viewActivity(resourceEvent.getActivity(), user);
        return resourceEvent.setViewed(true);
    }

    void processViews(List<Post> posts, User user) {
        if (user == null) return;

        entityManager.flush();
        Map<Post, Post> postIndex =
            posts.stream()
                .collect(toMap(post -> post, post -> post));

        Map<Resource, ResourceEvent> referrals =
            getResourceEvents(posts, REFERRAL, user).stream()
                .collect(toMap(ResourceEvent::getResource, identity()));

        Map<Resource, ResourceEvent> responses =
            getResourceEvents(posts, RESPONSE, user).stream()
                .collect(toMap(ResourceEvent::getResource, identity()));

        for (Map.Entry<Post, Post> postIndexEntry : postIndex.entrySet()) {
            Post post = postIndexEntry.getValue();
            post.setReferral(referrals.get(post));
            post.setResponse(responses.get(post));
        }
    }

    private ResourceEvent getResourceEvent(Resource resource, hr.prism.board.enums.ResourceEvent event, User user) {
        List<Long> ids =
            resourceEventRepository.findMaxIdsByResourcesAndEventAndUser(singletonList(resource), event, user);

        if (ids.isEmpty()) return null;
        return ofNullable(resourceEventRepository.findOne(ids.get(0)))
            .map(resourceEvent -> resourceEvent.setExposeResponseData(Objects.equals(user, resourceEvent.getUser())))
            .orElse(null);
    }

    private <T extends Resource> List<ResourceEvent> getResourceEvents(
        List<T> resources, hr.prism.board.enums.ResourceEvent event, User user) {
        List<Long> ids = resourceEventRepository.findMaxIdsByResourcesAndEventAndUser(resources, event, user);

        if (ids.isEmpty()) return emptyList();
        return resourceEventRepository.findOnes(ids).stream()
            .map(resourceEvent -> resourceEvent.setExposeResponseData(Objects.equals(user, resourceEvent.getUser())))
            .collect(Collectors.toList());
    }

    private void createPostReferral(Post post, User user) {
        String referral = sha256Hex(randomUUID().toString());
        DemographicDataStatus demographicDataStatus = getDemographicDataStatus(user, post);
        resourceEventRepository.save(
            new ResourceEvent()
                .setResource(post)
                .setEvent(REFERRAL)
                .setUser(user)
                .setRole(demographicDataStatus.getRole())
                .setReferral(referral));
    }

    private DemographicDataStatus getDemographicDataStatus(User user, Post post) {
        Department department = (Department) post.getParent().getParent();
        return departmentUserService.makeDemographicDataStatus(user, department);
    }

    private void updateResourceEventSummaries(Post post, Role role) {
        if (role == ADMINISTRATOR) return;

        Map<hr.prism.board.enums.ResourceEvent, ResourceEventSummary> summaries = getResourceEventSummaries(post);
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
                    throw new IllegalStateException("Unsupported event type: " + summaryKey);
            }
        }
    }

    private Map<hr.prism.board.enums.ResourceEvent, ResourceEventSummary> getResourceEventSummaries(Post post) {
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
        return summaries;
    }

    private void sendResponseNotifications(ResourceEvent resourceEvent) {
        Long responseId = resourceEvent.getId();
        Long postId = resourceEvent.getResource().getId();
        Document documentResume = resourceEvent.getDocumentResume();

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
                            new Notification.Attachment()
                                .setName(documentResume.getFileName())
                                .setUrl(documentResume.getCloudinaryUrl())
                                .setLabel("Application")))));
    }

}
