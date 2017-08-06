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

        if (resourceEvent == null || LocalDateTime.now().minusSeconds(viewIntervalSeconds).isAfter(resourceEvent.getCreatedTimestamp())) {
            return resourceEventRepository.save(new ResourceEvent().setResource(resource).setEvent(hr.prism.board.enums.ResourceEvent.VIEW).setUser(user));
        }

        return resourceEvent;
    }

    public ResourceEvent getOrCreateResourceResponse(Long resourceId, hr.prism.board.enums.ResourceEvent event, DocumentDTO resumeDTO, String website, String coveringNote) {
        User user = userService.getCurrentUserSecured();
        if (hr.prism.board.enums.ResourceEvent.REFERRAL_EVENTS.contains(event)) {

        }

        return null;
    }

}
