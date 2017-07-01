package hr.prism.board.service.event;

import com.google.common.collect.HashMultimap;
import hr.prism.board.domain.*;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.RedirectAction;
import hr.prism.board.enums.State;
import hr.prism.board.event.NotificationEvent;
import hr.prism.board.service.*;
import hr.prism.board.service.cache.UserCacheService;
import hr.prism.board.util.BoardUtils;
import hr.prism.board.workflow.Notification;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

import javax.inject.Inject;
import javax.transaction.Transactional;
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

    public void publishEvent(Object source, List<Notification> notifications) {
        applicationEventPublisher.publishEvent(new NotificationEvent(source, notifications));
    }

    public void publishEvent(Object source, Long resourceId, List<Notification> notifications, State state) {
        applicationEventPublisher.publishEvent(new NotificationEvent(source, resourceId, notifications, state));
    }

    public void publishEvent(Object source, Long creatorId, Long resourceId, List<Notification> notifications) {
        applicationEventPublisher.publishEvent(new NotificationEvent(source, creatorId, resourceId, notifications));
    }

    public void publishEvent(Object source, Long creatorId, Long resourceId, Action action, List<Notification> notifications, State state) {
        applicationEventPublisher.publishEvent(new NotificationEvent(source, creatorId, resourceId, action, notifications, state));
    }

    @Async
    @TransactionalEventListener
    public void sendNotificationsAsync(NotificationEvent notificationEvent) {
        sendNotifications(notificationEvent);
    }

    protected void sendNotifications(NotificationEvent notificationEvent) {
        User creator = null;
        Long creatorId = notificationEvent.getCreatorId();
        if (creatorId != null) {
            creator = userCacheService.findOne(notificationEvent.getCreatorId());
        }

        Resource resource = null;
        Long resourceId = notificationEvent.getResourceId();
        if (resourceId != null) {
            resource = resourceService.findOne(resourceId);
        }

        HashMultimap<User, String> sent = HashMultimap.create();
        List<Notification> notifications = notificationEvent.getNotifications();

        State state = notificationEvent.getState();
        Action action  = notificationEvent.getAction();
        for (Notification notification : notifications) {
            State notificationState = notification.getState();
            if (notificationState == null || Objects.equals(state, notificationState)) {
                List<User> recipients = makeRecipients(creator, resource, notification);

                if (!recipients.isEmpty()) {
                    String template = notification.getTemplate();
                    Map<String, String> parameters = makeParameters(resource, state, action, notification);

                    for (User recipient : recipients) {
                        if (!sent.containsEntry(recipient, template)) {
                            RedirectAction redirectAction = RedirectAction.makeForUser(recipient);
                            if (template.startsWith("reset") || template.startsWith("reject")) {
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

    private List<User> makeRecipients(User creator, Resource resource, Notification notification) {
        Long userId = notification.getUserId();
        List<User> recipients = new ArrayList<>();
        if (userId != null) {
            recipients.add(userCacheService.findOne(userId));
        } else if (notification.isFilteringByCategory()) {
            recipients.addAll(userService.findByResourceAndEnclosingScopeAndRoleAndCategories(resource, notification.getScope(), notification.getRole()));
        } else {
            recipients.addAll(userService.findByResourceAndEnclosingScopeAndRole(resource, notification.getScope(), notification.getRole()));
        }

        if (creator != null && notification.isExcludingCreator()) {
            recipients.remove(creator);
        }

        return recipients;
    }

    private Map<String, String> makeParameters(Resource resource, State state, Action action, Notification notification) {
        String template = notification.getTemplate();
        Map<String, String> parameters = new HashMap<>();
        if (resource != null) {
            resource.getParents().stream().map(ResourceRelation::getResource1).forEach(parent -> parameters.put(parent.getScope().name().toLowerCase(), parent.getName()));
            if (action != null) {
                if (action == Action.ACCEPT) {
                    if (state == State.ACCEPTED) {
                        parameters.put("publicationSchedule", "immediately");
                    } else {
                        String liveTimestamp = postService.getEffectiveLiveTimestamp((Post) resource).format(BoardUtils.DATETIME_FORMATTER);
                        parameters.put("publicationSchedule", "on or around " + liveTimestamp + ". We will send you a follow-up message when your post has gone live");
                    }
                }

                if (action.isRequireComment()) {
                    ResourceOperation resourceOperation = resourceService.getLatestResourceOperation(action);
                    parameters.put("comment", resourceOperation.getComment());
                }
            }
        }

        Map<String, String> customParameters = notification.getCustomParameters();
        if (customParameters != null) {
            parameters.putAll(customParameters);
        }

        return parameters;
    }

}
