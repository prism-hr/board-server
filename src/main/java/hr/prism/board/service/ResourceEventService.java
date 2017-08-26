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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
        verifyEventIdentifiable(user, ipAddress);
        return saveResourceEvent(post, new ResourceEvent().setResource(post).setEvent(hr.prism.board.enums.ResourceEvent.VIEW).setUser(user).setIpAddress(ipAddress));
    }

    public ResourceEvent createPostReferral(Post post, User user, String ipAddress) {
        verifyEventIdentifiable(user, ipAddress);
        return saveResourceEvent(post, new ResourceEvent().setResource(post).setEvent(hr.prism.board.enums.ResourceEvent.REFERRAL)
            .setUser(user).setIpAddress(ipAddress).setReferral(DigestUtils.sha256Hex(UUID.randomUUID().toString())));
    }

    public ResourceEvent getOrCreatePostResponse(Post post, User user, ResourceEventDTO resourceEventDTO) {
        if (post.getApplyEmail() == null) {
            throw new BoardException(ExceptionCode.INVALID_RESOURCE_EVENT);
        }

        DocumentDTO documentResumeDTO = resourceEventDTO.getDocumentResume();
        String websiteResume = resourceEventDTO.getWebsiteResume();
        String coveringNote = resourceEventDTO.getCoveringNote();

        ResourceEvent previousResponse = findByResourceAndEventAndUser(post, hr.prism.board.enums.ResourceEvent.RESPONSE, user);
        if (previousResponse != null) {
            throw new BoardDuplicateException(ExceptionCode.DUPLICATE_RESOURCE_EVENT, previousResponse.getId());
        }

        Document documentResume = null;
        if (documentResumeDTO != null) {
            documentResume = documentService.getOrCreateDocument(documentResumeDTO);
        }

        ResourceEvent response = saveResourceEvent(post, new ResourceEvent().setResource(post).setEvent(hr.prism.board.enums.ResourceEvent.RESPONSE)
            .setUser(user).setDocumentResume(documentResume).setWebsiteResume(websiteResume).setCoveringNote(coveringNote));
        if (BooleanUtils.isTrue(resourceEventDTO.getDefaultResume())) {
            userService.updateUserResume(user, documentResume, websiteResume);
        }

        Long postId = post.getId();
        hr.prism.board.workflow.Activity activity = new hr.prism.board.workflow.Activity()
            .setScope(Scope.POST).setRole(Role.ADMINISTRATOR).setExcludingCreator(true).setActivity(hr.prism.board.enums.Activity.RESPOND_POST_ACTIVITY);
        activityEventService.publishEvent(this, postId, response, Collections.singletonList(activity));

        if (post.getApplyEmail().equals(user.getEmail())) {
            hr.prism.board.workflow.Notification notification = new hr.prism.board.workflow.Notification()
                .setScope(Scope.POST).setRole(Role.ADMINISTRATOR).setExcludingCreator(true).setNotification(Notification.RESPOND_POST_NOTIFICATION);
            notificationEventService.publishEvent(this, postId, Collections.singletonList(notification));
        } else {
            // TODO: send the email / attachment to the specified address
        }

        return response;
    }

    public ResourceEvent findByResourceAndEventAndUser(Resource resource, hr.prism.board.enums.ResourceEvent event, User user) {
        return resourceEventRepository.findByResourceAndEventAndUser(resource, event, user);
    }

    public List<ResourceEvent> findByResourceAndEvent(Resource resource, hr.prism.board.enums.ResourceEvent event) {
        return resourceEventRepository.findByResourceAndEvent(resource, event);
    }

    public List<ResourceEvent> findByResourceIdsAndEventAndUser(List<Long> resourceIds, hr.prism.board.enums.ResourceEvent event, User user) {
        return resourceEventRepository.findByResourceIdsAndEventAndUser(resourceIds, event, user);
    }

    public ResourceEvent getAndConsumeReferral(String referral) {
        ResourceEvent resourceEvent = resourceEventRepository.findByReferral(referral);
        if (resourceEvent == null) {
            // Bad referral, or referral consumed already - client should request a new one
            throw new BoardForbiddenException(ExceptionCode.FORBIDDEN_REFERRAL);
        }

        resourceEvent.setReferral(null);
        return resourceEventRepository.update(resourceEvent);
    }

    private void verifyEventIdentifiable(User user, String ipAddress) {
        if (user == null && ipAddress == null) {
            throw new BoardException(ExceptionCode.UNIDENTIFIABLE_RESOURCE_EVENT);
        }
    }

    private ResourceEvent saveResourceEvent(Post post, ResourceEvent resourceEvent) {
        resourceEvent = resourceEventRepository.save(resourceEvent);
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

        return resourceEvent;
    }

}
