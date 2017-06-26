package hr.prism.board.service.event;

import com.google.common.collect.HashMultimap;
import hr.prism.board.domain.*;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.RedirectAction;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
    private Environment environment;

    @Inject
    private ApplicationEventPublisher applicationEventPublisher;

    public void publishEvent(Object source, Long resourceId, List<Notification> notifications, State state) {
        applicationEventPublisher.publishEvent(new NotificationEvent(source, resourceId, notifications, state));
    }

    public void publishEvent(Object source, Long creatorId, Long resourceId, List<Notification> notifications) {
        applicationEventPublisher.publishEvent(new NotificationEvent(source, creatorId, resourceId, notifications, null));
    }

    public void publishEvent(Object source, Long creatorId, Long resourceId, List<Notification> notifications, State state) {
        applicationEventPublisher.publishEvent(new NotificationEvent(source, creatorId, resourceId, notifications, state));
    }

    @Async
    @TransactionalEventListener
    public void sendNotificationsAsync(NotificationEvent notificationEvent) {
        sendNotifications(notificationEvent);
    }

    protected void sendNotifications(NotificationEvent notificationEvent) {
        Long resourceId = notificationEvent.getResourceId();
        Resource resource = resourceService.findOne(resourceId);
        User creator = userCacheService.findOne(notificationEvent.getCreatorId());

        HashMultimap<User, String> sent = HashMultimap.create();
        List<Notification> notifications = notificationEvent.getNotifications();

        State state = notificationEvent.getState();
        for (Notification notification : notifications) {
            State notificationState = notification.getState();
            if (notificationState == null || Objects.equals(state, notificationState)) {
                Long userId = notification.getUserId();
                List<User> recipients = new ArrayList<>();
                if (userId == null) {
                    // TODO: filter by category for the publish notification
                    recipients.addAll(userService.findByResourceAndEnclosingScopeAndRole(resource, notification.getScope(), notification.getRole()));
                } else {
                    recipients.add(userCacheService.findOne(userId));
                }

                if (creator != null && notification.isExcludingCreator()) {
                    recipients.remove(creator);
                }

                if (!recipients.isEmpty()) {
                    String template = notification.getTemplate();
                    Map<String, String> parameters = new HashMap<>();

                    resource.getParents().stream().map(ResourceRelation::getResource1).forEach(parent -> parameters.put(parent.getScope().name().toLowerCase(), parent.getName()));
                    if ("approve_post".equals(template)) {
                        LocalDateTime liveTimestamp = postService.getEffectiveLiveTimestamp((Post) resource);
                        parameters.put("liveTimestamp", liveTimestamp.format(DateTimeFormatter.ofPattern("dd/MM/YYYY")));
                    } else if ("suspend_post".equals(template)) {
                        ResourceOperation resourceOperation = resourceService.getLatestResourceOperation(Action.SUSPEND);
                        parameters.put("comment", resourceOperation.getComment());
                    } else if ("reject_post".equals(template)) {
                        ResourceOperation resourceOperation = resourceService.getLatestResourceOperation(Action.REJECT);
                        parameters.put("comment", resourceOperation.getComment());
                    }

                    for (User recipient : recipients) {
                        if (!sent.containsEntry(recipient, template)) {
                            RedirectAction redirectAction = RedirectAction.makeForUser(recipient);
                            if (template.startsWith("reject")) {
                                parameters.put("redirectUrl", RedirectService.makeForHome(environment.getProperty("server.url"), redirectAction));
                            } else {
                                parameters.put("redirectUrl", RedirectService.makeForResource(environment.getProperty("server.url"), resourceId, redirectAction));
                            }

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
