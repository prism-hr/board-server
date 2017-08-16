package hr.prism.board.service;

import hr.prism.board.domain.*;
import hr.prism.board.dto.DocumentDTO;
import hr.prism.board.dto.ResourceEventDTO;
import hr.prism.board.exception.BoardDuplicateException;
import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.repository.ResourceEventRepository;
import hr.prism.board.value.ResourceEventSummary;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ResourceEventService {

    @Inject
    private ResourceEventRepository resourceEventRepository;

    @Inject
    private UserService userService;

    @Inject
    private DocumentService documentService;

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${view.interval.seconds}")
    private Integer viewIntervalSeconds;

    public ResourceEvent findOne(Long resourceEventId) {
        return resourceEventRepository.findOne(resourceEventId);
    }

    public ResourceEvent getOrCreatePostView(Post post, User user, String ipAddress) {
        ResourceEvent lastResourceEvent = getLastResourceEvent(post, hr.prism.board.enums.ResourceEvent.VIEW, user, ipAddress);
        if (lastResourceEvent == null || isNewEvent(hr.prism.board.enums.ResourceEvent.VIEW, lastResourceEvent)) {
            return saveResourceEvent(post, new ResourceEvent().setResource(post).setEvent(hr.prism.board.enums.ResourceEvent.VIEW).setUser(user));
        }

        throw new BoardDuplicateException(ExceptionCode.DUPLICATE_POST_RESPONSE, lastResourceEvent.getId());
    }

    public ResourceEvent getOrCreatePostResponse(Post post, User user, String ipAddress, ResourceEventDTO resourceEventDTO) {
        ResourceEvent lastResourceEvent = getLastResourceEvent(post, hr.prism.board.enums.ResourceEvent.RESPONSE, user, ipAddress);
        if (lastResourceEvent == null || isNewEvent(hr.prism.board.enums.ResourceEvent.RESPONSE, lastResourceEvent)) {
            ResourceEvent resourceEvent = new ResourceEvent().setResource(post).setEvent(hr.prism.board.enums.ResourceEvent.RESPONSE).setUser(user);
            if (resourceEventDTO != null) {
                DocumentDTO documentResumeDTO = resourceEventDTO.getDocumentResume();
                String websiteResume = resourceEventDTO.getWebsiteResume();
                String coveringNote = resourceEventDTO.getCoveringNote();

                String applyEmail = post.getApplyEmail();
                if (applyEmail != null || BooleanUtils.isTrue(post.getForwardCandidates())) {
                    if ((documentResumeDTO != null || websiteResume != null) && coveringNote != null) {
                        Document documentResume = null;
                        if (documentResumeDTO != null) {
                            documentResume = documentService.getOrCreateDocument(documentResumeDTO);
                        }

                        resourceEvent.setDocumentResume(documentResume);
                        resourceEvent.setWebsiteResume(websiteResume);
                        resourceEvent.setCoveringNote(coveringNote);

                        if (BooleanUtils.isTrue(resourceEventDTO.getDefaultResume())) {
                            userService.updateUserResume(user, documentResume, websiteResume);
                        }
                    } else if (applyEmail == null) {
                        throw new BoardException(ExceptionCode.INVALID_POST_RESPONSE);
                    }
                }
            }

            return saveResourceEvent(post, resourceEvent);
        }

        throw new BoardDuplicateException(ExceptionCode.DUPLICATE_POST_RESPONSE, lastResourceEvent.getId());
    }

    public List<ResourceEvent> getResourceEvents(Resource resource) {
        return resourceEventRepository.findByResourceOrderByIdDesc(resource);
    }

    public List<ResourceEvent> getResourceEvents(Resource resource, hr.prism.board.enums.ResourceEvent event) {
        return resourceEventRepository.findByResourceAndEventOrderByIdDesc(resource, event);
    }

    private ResourceEvent getLastResourceEvent(Resource post, hr.prism.board.enums.ResourceEvent event, User user, String ipAddress) {
        if (user == null) {
            return ipAddress == null ? null : resourceEventRepository.findFirstByResourceAndEventAndIpAddressOrderByIdDesc(post, event, ipAddress);
        }

        return resourceEventRepository.findFirstByResourceAndEventAndUserOrderByIdDesc(post, event, user);
    }

    private boolean isNewEvent(hr.prism.board.enums.ResourceEvent event, ResourceEvent lastResourceEvent) {
        return event == hr.prism.board.enums.ResourceEvent.RESPONSE && lastResourceEvent.getDocumentResume() == null && lastResourceEvent.getWebsiteResume() == null
            || LocalDateTime.now().minusSeconds(viewIntervalSeconds).isAfter(lastResourceEvent.getCreatedTimestamp());
    }

    private ResourceEvent saveResourceEvent(Post post, ResourceEvent resourceEvent) {
        resourceEvent = resourceEventRepository.save(resourceEvent);
        entityManager.flush();
        for (ResourceEventSummary summary : resourceEventRepository.findSummaryByResource(post)) {
            if (summary.getKey() == hr.prism.board.enums.ResourceEvent.VIEW) {
                post.setViewCount(summary.getCount());
                post.setLastViewTimestamp(summary.getLastTimestamp());
            } else {
                post.setResponseCount(summary.getCount());
                post.setLastResponseTimestamp(summary.getLastTimestamp());
            }
        }

        return resourceEvent;
    }

}
