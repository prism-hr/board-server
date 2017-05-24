package hr.prism.board.service;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.TreeMultimap;
import hr.prism.board.domain.*;
import hr.prism.board.event.NotificationEvent;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class NotificationEventService {
    
    @Inject
    private ResourceService resourceService;
    
    @Inject
    private UserRoleService userRoleService;
    
    @Inject
    private NotificationService notificationService;
    
    @Async
    @TransactionalEventListener
    public void sendNotifications(NotificationEvent notificationEvent) {
        Resource resource = resourceService.findOne(notificationEvent.getResourceId());
        
        TreeMultimap<Scope, User> index = TreeMultimap.create();
        List<UserRole> userRoles = userRoleService.findInEnclosingScopeByResourceAndUserAndRole(resource, notificationEvent.getRole());
        userRoles.forEach(userRole -> index.put(userRole.getResource().getScope(), userRole.getUser()));
        
        Collection<User> recipients = null;
        for (Scope scope : index.keySet()) {
            recipients = index.get(scope);
            if (CollectionUtils.isNotEmpty(recipients)) {
                break;
            }
        }
        
        if (recipients == null) {
            return;
        }
        
        ImmutableMap.Builder<String, String> parameterBuilder = ImmutableMap.<String, String>builder()
            .put("creator", notificationEvent.getCreator());
        resource.getParents().stream().map(ResourceRelation::getResource1).filter(parent -> !parent.equals(resource))
            .forEach(parent -> parameterBuilder.put(parent.getScope().name().toLowerCase(), parent.getName()));
        Map<String, String> parameters = parameterBuilder.build();
        
        Long creatorId = notificationEvent.getCreatorId();
        for (User recipient : recipients) {
            if (!recipient.getId().equals(creatorId)) {
                NotificationService.Notification notification = notificationService.makeNotification(notificationEvent.getNotification(), recipient, parameters);
                notificationService.sendNotification(notification);
            }
        }
    }
}
