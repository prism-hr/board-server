package hr.prism.board.service;

import hr.prism.board.domain.*;
import hr.prism.board.dto.DocumentDTO;
import hr.prism.board.dto.ResourceEventDTO;
import hr.prism.board.enums.Notification;
import hr.prism.board.enums.Role;
import hr.prism.board.enums.Scope;
import hr.prism.board.exception.BoardDuplicateException;
import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.repository.ResourceEventRepository;
import hr.prism.board.service.event.ActivityEventService;
import hr.prism.board.service.event.NotificationEventService;
import hr.prism.board.value.ResourceEventSummary;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class ResourceEventService {

    @Inject
    private ResourceEventRepository resourceEventRepository;

    @Inject
    private DocumentService documentService;

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

    public ResourceEvent getOrCreatePostView(Post post, User user, String ipAddress) {
        verifyEventIdentifiable(user, ipAddress);
        return saveResourceEvent(post, new ResourceEvent().setResource(post).setEvent(hr.prism.board.enums.ResourceEvent.VIEW).setUser(user).setIpAddress(ipAddress));
    }

    public ResourceEvent getOrCreatePostReferral(Post post, User user, String ipAddress) {
        verifyEventIdentifiable(user, ipAddress);
        return saveResourceEvent(post, new ResourceEvent().setResource(post).setEvent(hr.prism.board.enums.ResourceEvent.REFERRAL).setUser(user).setIpAddress(ipAddress));
    }

    public ResourceEvent getOrCreatePostResponse(Post post, User user, ResourceEventDTO resourceEventDTO) {
        DocumentDTO documentResumeDTO = resourceEventDTO.getDocumentResume();
        String websiteResume = resourceEventDTO.getWebsiteResume();
        String coveringNote = resourceEventDTO.getCoveringNote();
        if (documentResumeDTO == null && websiteResume == null && post.getApplyEmail() != null && coveringNote == null) {
            throw new BoardException(ExceptionCode.INVALID_RESOURCE_EVENT);
        }

        ResourceEvent previousResponse = resourceEventRepository.findByResourceAndEventAndUser(post, hr.prism.board.enums.ResourceEvent.RESPONSE, user);
        if (previousResponse != null) {
            throw new BoardDuplicateException(ExceptionCode.DUPLICATE_RESOURCE_EVENT, previousResponse.getId());
        }

        Document documentResume = null;
        if (documentResumeDTO != null) {
            documentResume = documentService.getOrCreateDocument(documentResumeDTO);
        }

        ResourceEvent response = resourceEventRepository.save(new ResourceEvent().setResource(post).setEvent(hr.prism.board.enums.ResourceEvent.RESPONSE)
            .setUser(user).setDocumentResume(documentResume).setWebsiteResume(websiteResume).setCoveringNote(coveringNote));

        Long postId = post.getId();
        hr.prism.board.workflow.Activity activity = new hr.prism.board.workflow.Activity()
            .setScope(Scope.POST).setRole(Role.ADMINISTRATOR).setExcludingCreator(true).setActivity(hr.prism.board.enums.Activity.RESPOND_POST_ACTIVITY);
        activityEventService.publishEvent(this, postId, response, Collections.singletonList(activity));

        hr.prism.board.workflow.Notification notification = new hr.prism.board.workflow.Notification()
            .setScope(Scope.POST).setRole(Role.ADMINISTRATOR).setExcludingCreator(true).setNotification(Notification.RESPOND_POST_NOTIFICATION);
        notificationEventService.publishEvent(this, postId, Collections.singletonList(notification));
        return response;
    }

    public List<ResourceEvent> getResourceEvents(Resource resource, hr.prism.board.enums.ResourceEvent event) {
        return resourceEventRepository.findByResourceAndEventOrderByIdDesc(resource, event);
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
                    post.setLastResponseTimestamp(summary.getLastTimestamp());
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
