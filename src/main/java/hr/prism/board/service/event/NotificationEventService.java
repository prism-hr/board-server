package hr.prism.board.service.event;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import hr.prism.board.domain.*;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.State;
import hr.prism.board.event.NotificationEvent;
import hr.prism.board.service.*;
import hr.prism.board.service.cache.UserCacheService;
import hr.prism.board.workflow.Notification;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    private PostService postService;

    @Inject
    private ObjectMapper objectMapper;

    @Inject
    private Environment environment;

    @Inject
    private ApplicationEventPublisher applicationEventPublisher;

    public void publishEvent(Object source, Long resourceId, String notification, State state) {
        applicationEventPublisher.publishEvent(new NotificationEvent(source, resourceId, notification, state));
    }

    public void publishEvent(Object source, Long creatorId, Long resourceId, String notification, State state) {
        applicationEventPublisher.publishEvent(new NotificationEvent(source, creatorId, resourceId, notification, state));
    }

    @Async
    @TransactionalEventListener
    public void sendNotificationsAsync(NotificationEvent notificationEvent) {
        sendNotifications(notificationEvent);
    }

    protected void sendNotifications(NotificationEvent notificationEvent) {
        User creator = userCacheService.findOne(notificationEvent.getCreatorId());
        Resource resource = resourceService.findOne(notificationEvent.getResourceId());

        List<Notification> notifications;
        HashMultimap<User, String> sent = HashMultimap.create();
        try {
            notifications = objectMapper.readValue(notificationEvent.getNotification(), new TypeReference<List<Notification>>() {
            });
        } catch (IOException e) {
            throw new IllegalStateException("Could not deserialize notifications");
        }

        State state = notificationEvent.getState();
        for (Notification notification : notifications) {
            State notificationState = notification.getState();
            if (notificationState == null || Objects.equals(state, notificationState)) {
                List<User> recipients = userService.findByResourceAndEnclosingScopeAndRole(resource, notification.getScope(), notification.getRole());
                if (creator != null && notification.isExcludingCreator()) {
                    recipients.remove(creator);
                }

                if (!recipients.isEmpty()) {
                    String template = notification.getTemplate();
                    ImmutableMap.Builder<String, String> parameterBuilder = ImmutableMap.builder();

                    String redirectUrl = null;
                    resource.getParents().stream().map(ResourceRelation::getResource1).forEach(p -> parameterBuilder.put(p.getScope().name().toLowerCase(), p.getName()));
                    if ("approve_post".equals(template)) {
                        LocalDateTime liveTimestamp = postService.getEffectiveLiveTimestamp((Post) resource);
                        parameterBuilder.put("liveTimestamp", liveTimestamp.format(DateTimeFormatter.ofPattern("dd/MM/YYYY")));
                    } else if ("suspend_post".equals(template)) {
                        ResourceOperation resourceOperation = resourceService.getLatestResourceOperation(Action.SUSPEND);
                        parameterBuilder.put("comment", resourceOperation.getComment());
                    } else if ("reject_post".equals(template)) {
                        redirectUrl = RedirectService.makeRedirectForLogin(environment.getProperty("server.url"));
                        ResourceOperation resourceOperation = resourceService.getLatestResourceOperation(Action.REJECT);
                        parameterBuilder.put("comment", resourceOperation.getComment());
                    }

                    if (redirectUrl == null) {
                        redirectUrl = RedirectService.makeRedirectForResource(environment.getProperty("server.url"), resource);
                    }

                    parameterBuilder.put("redirectUrl", redirectUrl);
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

}
