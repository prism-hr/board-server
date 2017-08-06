package hr.prism.board.service;

import hr.prism.board.domain.Resource;
import hr.prism.board.domain.ResourceEvent;
import hr.prism.board.domain.User;
import hr.prism.board.dto.DocumentDTO;
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
        ResourceEvent resourceEvent;
        if (user == null) {
            if (ipAddress == null) {
                // No user or IP address, no way to throttle events, return null
                return null;
            }

            resourceEvent = resourceEventRepository.findFirstByResourceAndEventAndIpAddressOrderByIdDesc(resource, hr.prism.board.enums.ResourceEvent.VIEW, ipAddress);
        } else {
            resourceEvent = resourceEventRepository.findFirstByResourceAndEventAndUserOrderByIdDesc(resource, hr.prism.board.enums.ResourceEvent.VIEW, user);
        }

        if (resourceEvent == null || isNewEvent(resourceEvent)) {
            return resourceEventRepository.save(new ResourceEvent().setResource(resource).setEvent(hr.prism.board.enums.ResourceEvent.VIEW).setUser(user));
        }

        return resourceEvent;
    }

    public ResourceEvent getOrCreateResourceResponse(Long resourceId, hr.prism.board.enums.ResourceEvent event, DocumentDTO resumeDTO, String website, String coveringNote) {
        if (hr.prism.board.enums.ResourceEvent.RESPONSE_EVENTS.contains(event)) {
            User user = userService.getCurrentUserSecured();
            Resource resource = resourceService.findOne(resourceId);
            ResourceEvent resourceEvent = resourceEventRepository.findFirstByResourceAndEventAndUserOrderByIdDesc(resource, event, user);
            if (resourceEvent == null || hr.prism.board.enums.ResourceEvent.REFERRAL_EVENTS.contains(event) && isNewEvent(resourceEvent)) {
                return resourceEventRepository.save(new ResourceEvent().setResource(resource).setEvent(event).setUser(user));
            }

            return resourceEvent;
        }

        throw new UnsupportedOperationException("Event: " + event + " not a valid response event");
    }

    private boolean isNewEvent(ResourceEvent resourceEvent) {
        return LocalDateTime.now().minusSeconds(viewIntervalSeconds).isAfter(resourceEvent.getCreatedTimestamp());
    }

}
