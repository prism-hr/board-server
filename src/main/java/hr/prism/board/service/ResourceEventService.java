package hr.prism.board.service;

import hr.prism.board.domain.*;
import hr.prism.board.dto.DocumentDTO;
import hr.prism.board.dto.ResourceEventDTO;
import hr.prism.board.enums.Notification;
import hr.prism.board.enums.Role;
import hr.prism.board.enums.Scope;
import hr.prism.board.exception.BoardDuplicateException;
import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.BoardForbiddenException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.repository.ResourceEventRepository;
import hr.prism.board.service.event.ActivityEventService;
import hr.prism.board.service.event.NotificationEventService;
import hr.prism.board.value.ResourceEventSummary;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ResourceEventService {

    @Inject
    private ResourceEventRepository resourceEventRepository;

    @Inject
    private DocumentService documentService;

    @Inject
    private UserService userService;

    @Inject
    private ResourceService resourceService;

    @Lazy
    @Inject
    private ActivityEventService activityEventService;

    @Lazy
    @Inject
    private NotificationEventService notificationEventService;

    @PersistenceContext
    private EntityManager entityManager;

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
        ResourceEvent response = saveResourceEvent(post, new ResourceEvent().setResource(post).setEvent(hr.prism.board.enums.ResourceEvent.RESPONSE)
            .setUser(user).setDocumentResume(documentResume).setWebsiteResume(websiteResume).setCoveringNote(coveringNote)
            .setVisibleToAdministrator(resourceService.isResourceAdministrator(post, post.getApplyEmail())));
        if (BooleanUtils.isTrue(resourceEventDTO.getDefaultResume())) {
            userService.updateUserResume(user, documentResume, websiteResume);
        }

        Long postId = post.getId();
        hr.prism.board.workflow.Activity activity = new hr.prism.board.workflow.Activity()
            .setScope(Scope.POST).setRole(Role.ADMINISTRATOR).setActivity(hr.prism.board.enums.Activity.RESPOND_POST_ACTIVITY);
        activityEventService.publishEvent(this, postId, response, Collections.singletonList(activity));

        hr.prism.board.workflow.Notification notification = new hr.prism.board.workflow.Notification().setNotification(Notification.RESPOND_POST_NOTIFICATION).addAttachment(
            new hr.prism.board.workflow.Notification.Attachment().setName(documentResume.getFileName()).setUrl(documentResume.getCloudinaryUrl()).setLabel("Application"));
        notificationEventService.publishEvent(this, postId, response.getId(), Collections.singletonList(notification));
        return response;
    }

    public ResourceEvent findByResourceAndEventAndUser(Resource resource, hr.prism.board.enums.ResourceEvent event, User user) {
        List<Long> ids = resourceEventRepository.findMaxIdsByResourcesAndEventAndUser(Collections.singletonList(resource), event, user);
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

        resourceEvent.setReferral(null);
        resourceEvent = resourceEventRepository.update(resourceEvent);
        entityManager.flush();

        updateResourceEventSummary((Post) resourceEvent.getResource());
        return resourceEvent;
    }

    public void updateVisibleToAdministrator(Resource resource) {
        resourceEventRepository.updateVisibleToAdministrator(resource, hr.prism.board.enums.ResourceEvent.RESPONSE, true);
    }

    public List<ResourceEvent> findByIpAddresses(Collection<String> ipAddresses) {
        return resourceEventRepository.findByEventAndIpAddresses(hr.prism.board.enums.ResourceEvent.VIEW, ipAddresses);
    }

    private ResourceEvent saveResourceEvent(Post post, ResourceEvent resourceEvent) {
        resourceEvent = resourceEventRepository.save(resourceEvent);
        entityManager.flush();

        updateResourceEventSummary(post);
        return resourceEvent;
    }

    private void updateResourceEventSummary(Post post) {
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
