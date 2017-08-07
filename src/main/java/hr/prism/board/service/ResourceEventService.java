package hr.prism.board.service;

import hr.prism.board.domain.Post;
import hr.prism.board.domain.Resource;
import hr.prism.board.domain.ResourceEvent;
import hr.prism.board.domain.User;
import hr.prism.board.dto.ResourceEventDTO;
import hr.prism.board.repository.ResourceEventRepository;
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
    private ActionService actionService;

    @Inject
    private ResourceService resourceService;

    @Inject
    private UserService userService;

    @Value("${view.interval.seconds}")
    private Integer viewIntervalSeconds;

    public ResourceEvent getOrCreateResourceView(Resource resource, User user, String ipAddress) {
        ResourceEvent lastResourceEvent;
        if (user == null) {
            if (ipAddress == null) {
                return null;
            }

            lastResourceEvent = resourceEventRepository.findFirstByResourceAndEventAndIpAddressOrderByIdDesc(resource, hr.prism.board.enums.ResourceEvent.VIEW, ipAddress);
        } else {
            lastResourceEvent = resourceEventRepository.findFirstByResourceAndEventAndUserOrderByIdDesc(resource, hr.prism.board.enums.ResourceEvent.VIEW, user);
        }

        if (lastResourceEvent == null || isNewEvent(lastResourceEvent)) {
            return resourceEventRepository.save(new ResourceEvent().setResource(resource).setEvent(hr.prism.board.enums.ResourceEvent.VIEW).setUser(user));
        }

        return lastResourceEvent;
    }

    public ResourceEvent getOrCreatePostResponse(Long resourceId, ResourceEventDTO resourceEventDTO) {
        hr.prism.board.enums.ResourceEvent event;
        Post post = (Post) resourceService.findOne(resourceId);
        if (post.getApplyWebsite() != null) {
            event = hr.prism.board.enums.ResourceEvent.CLICK;
        } else if (post.getApplyDocument() != null) {
            event = hr.prism.board.enums.ResourceEvent.DOWNLOAD;
        } else {
            event = hr.prism.board.enums.ResourceEvent.EMAIL;
        }

        User user = userService.getCurrentUserSecured();
        ResourceEvent lastResourceEvent = resourceEventRepository.findFirstByResourceAndEventAndUserOrderByIdDesc(post, event, user);
        if (lastResourceEvent == null || isNewEvent(lastResourceEvent)) {
            return resourceEventRepository.save(new ResourceEvent().setResource(post).setEvent(event).setUser(user));
        }

        return lastResourceEvent;
    }

    private boolean isNewEvent(ResourceEvent lastResourceEvent) {
        return LocalDateTime.now().minusSeconds(viewIntervalSeconds).isAfter(lastResourceEvent.getCreatedTimestamp())
            || lastResourceEvent.getDocumentResume() == null && lastResourceEvent.getWebsite() == null;
    }

}
