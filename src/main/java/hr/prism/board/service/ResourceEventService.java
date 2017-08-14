package hr.prism.board.service;

import hr.prism.board.domain.*;
import hr.prism.board.dto.DocumentDTO;
import hr.prism.board.dto.ResourceEventDTO;
import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.repository.ResourceEventRepository;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.time.LocalDateTime;

@Service
@Transactional
public class ResourceEventService {

    @Inject
    private ResourceEventRepository resourceEventRepository;

    @Inject
    private UserService userService;

    @Inject
    private DocumentService documentService;

    @Value("${view.interval.seconds}")
    private Integer viewIntervalSeconds;

    public ResourceEvent getOrCreatePostView(Post post, User user, String ipAddress) {
        ResourceEvent lastResourceEvent = getLastResourceEvent(post, hr.prism.board.enums.ResourceEvent.VIEW, user, ipAddress);
        if (lastResourceEvent == null || isNewEvent(hr.prism.board.enums.ResourceEvent.VIEW, lastResourceEvent)) {
            return resourceEventRepository.save(new ResourceEvent().setResource(post).setEvent(hr.prism.board.enums.ResourceEvent.VIEW).setUser(user));
        }

        return lastResourceEvent;
    }

    // TODO: add support for user preferences
    public ResourceEvent getOrCreatePostResponse(Post post, User user, String ipAddress, ResourceEventDTO resourceEventDTO) {
        hr.prism.board.enums.ResourceEvent event;
        if (post.getApplyWebsite() != null) {
            event = hr.prism.board.enums.ResourceEvent.CLICK;
        } else if (post.getApplyDocument() != null) {
            event = hr.prism.board.enums.ResourceEvent.DOWNLOAD;
        } else {
            event = hr.prism.board.enums.ResourceEvent.EMAIL;
        }

        ResourceEvent lastResourceEvent = getLastResourceEvent(post, event, user, ipAddress);
        if (lastResourceEvent == null || isNewEvent(event, lastResourceEvent)) {
            ResourceEvent resourceEvent = new ResourceEvent().setResource(post).setEvent(event).setUser(user);
            if (BooleanUtils.isTrue(resourceEventDTO.getShare())) {
                DocumentDTO documentResumeDTO = resourceEventDTO.getDocumentResume();
                String websiteResume = resourceEventDTO.getWebsiteResume();
                String coveringNote = resourceEventDTO.getCoveringNote();
                if (documentResumeDTO == null && websiteResume == null || coveringNote == null) {
                    throw new BoardException(ExceptionCode.INVALID_POST_RESPONSE);
                }

                Document documentResume = null;
                if (documentResumeDTO != null) {
                    documentResume = documentService.getOrCreateDocument(documentResumeDTO);
                }

                resourceEvent.setDocumentResume(documentResume);
                resourceEvent.setWebsite(websiteResume);
                resourceEvent.setCoveringNote(coveringNote);

                if (BooleanUtils.isTrue(resourceEventDTO.getShareAsDefault())) {
                    userService.updateUserResume(user, documentResume, websiteResume);
                }
            }

            return resourceEventRepository.save(resourceEvent);
        }

        return lastResourceEvent;
    }

    private ResourceEvent getLastResourceEvent(Resource post, hr.prism.board.enums.ResourceEvent event, User user, String ipAddress) {
        if (user == null) {
            if (ipAddress == null) {
                return null;
            }

            return resourceEventRepository.findFirstByResourceAndEventAndIpAddressOrderByIdDesc(post, event, ipAddress);
        }

        return resourceEventRepository.findFirstByResourceAndEventAndUserOrderByIdDesc(post, event, user);
    }

    private boolean isNewEvent(hr.prism.board.enums.ResourceEvent event, ResourceEvent lastResourceEvent) {
        return event.isResponse() && lastResourceEvent.getDocumentResume() == null && lastResourceEvent.getWebsite() == null
            || LocalDateTime.now().minusSeconds(viewIntervalSeconds).isAfter(lastResourceEvent.getCreatedTimestamp());
    }

}
