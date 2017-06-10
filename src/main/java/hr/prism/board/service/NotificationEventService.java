package hr.prism.board.service;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.transaction.Transactional;

import hr.prism.board.domain.Resource;
import hr.prism.board.domain.ResourceRelation;
import hr.prism.board.domain.User;
import hr.prism.board.event.NotificationEvent;
import hr.prism.board.service.cache.UserCacheService;
import hr.prism.board.workflow.Notification;

@Service
@Transactional
public class NotificationEventService {

    @Inject
    private ResourceService resourceService;

    @Inject
    private NotificationService notificationService;

    @Inject
    private UserService userService;

    @Inject
    private UserCacheService userCacheService;

    @Inject
    private ObjectMapper objectMapper;

    @Inject
    private Environment environment;

    @Async
    @TransactionalEventListener
    public void sendNotifications(NotificationEvent notificationEvent) throws IOException {
        User creator = userCacheService.findOne(notificationEvent.getCreatorId());
        Resource resource = resourceService.findOne(notificationEvent.getResourceId());
        String creatorFullName = creator.getFullName();

        HashMultimap<User, String> sent = HashMultimap.create();
        List<Notification> notifications =
            objectMapper.readValue(notificationEvent.getNotification(), new TypeReference<List<Notification>>() {});
        for (Notification notification : notifications) {
            List<User> recipients = userService.findByResourceAndEnclosingScopeAndRole(resource, notification.getScope(), notification
                .getRole());
            if (notification.isExcludingCreator()) {
                recipients.remove(creator);
            }

            if (recipients.size() > 0) {
                String template = notification.getTemplate();
                ImmutableMap.Builder<String, String> parameterBuilder = ImmutableMap.<String, String>builder()
                    .put("creator", creatorFullName)
                    .put("redirectUrl", RedirectService.makeRedirectForResource(environment.getProperty("server.url"), resource));
                resource.getParents()
                    .stream()
                    .map(ResourceRelation::getResource1)
                    .filter(parent -> !parent.equals(resource))
                    .forEach(parent -> parameterBuilder.put(parent.getScope().name().toLowerCase(), parent.getName()));
                Map<String, String> parameters = parameterBuilder.build();

                for (User recipient : recipients) {
                    if (!sent.containsEntry(recipient, template)) {
                        NotificationService.NotificationInstance notificationInstance = notificationService.makeNotification(template, recipient, parameters);
                        notificationService.sendNotification(notificationInstance);
                        sent.put(recipient, template);
                    }
                }
            }
        }
    }

}
