package hr.prism.board.event.consumer;

import com.google.common.collect.HashMultimap;
import hr.prism.board.domain.Post;
import hr.prism.board.domain.Resource;
import hr.prism.board.domain.User;
import hr.prism.board.enums.Action;
import hr.prism.board.event.NotificationEvent;
import hr.prism.board.exception.BoardNotificationException;
import hr.prism.board.service.NotificationService;
import hr.prism.board.service.NotificationService.NotificationRequest;
import hr.prism.board.service.ResourceEventService;
import hr.prism.board.service.ResourceService;
import hr.prism.board.value.UserNotification;
import hr.prism.board.workflow.Notification;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class NotificationEventConsumer {

    private static Logger LOGGER = getLogger(NotificationEventConsumer.class);

    private final ResourceService resourceService;

    private final NotificationService notificationService;

    private final ResourceEventService resourceEventService;

    private final NotificationAttachmentMapper notificationAttachmentMapper;

    private final ApplicationContext applicationContext;

    public NotificationEventConsumer(ResourceService resourceService, NotificationService notificationService,
                                     ResourceEventService resourceEventService,
                                     NotificationAttachmentMapper notificationAttachmentMapper,
                                     ApplicationContext applicationContext) {
        this.resourceService = resourceService;
        this.notificationService = notificationService;
        this.resourceEventService = resourceEventService;
        this.notificationAttachmentMapper = notificationAttachmentMapper;
        this.applicationContext = applicationContext;
    }

    @Async
    @TransactionalEventListener
    public void consume(NotificationEvent notificationEvent) {
        Resource resource = getResource(notificationEvent);

        Action action = notificationEvent.getAction();
        List<Notification> notifications = notificationEvent.getNotifications();

        HashMultimap<User, hr.prism.board.enums.Notification> sent = HashMultimap.create();
        for (Notification notification : notifications) {
            hr.prism.board.enums.Notification template = notification.getNotification();

            List<UserNotification> recipients =
                applicationContext.getBean(template.getRecipients()).list(resource, notification);
            for (UserNotification recipient : recipients) {
                User user = recipient.getUser();
                if (!sent.containsEntry(user, template)) {
                    try {
                        notificationService.sendNotification(
                            new NotificationRequest(template, user, recipient.getInvitation(), resource, action,
                                notification.getAttachments().stream()
                                    .map(notificationAttachmentMapper)
                                    .collect(toList())));
                        sent.put(user, template);
                    } catch (BoardNotificationException e) {
                        LOGGER.info("Aborted sending notification: " +
                            template + " to " + user.toString() + " - " + e.getMessage());
                    }
                }
            }
        }
    }

    private Resource getResource(NotificationEvent notificationEvent) {
        Long resourceId = notificationEvent.getResourceId();
        if (resourceId == null) {
            return null;
        }

        Resource resource = resourceService.getById(resourceId);
        if (resource == null) {
            return null;
        }

        Long resourceEventId = notificationEvent.getResourceEventId();
        if (resourceEventId != null) {
            ((Post) resource).setResponse(resourceEventService.getById(resourceEventId));
        }

        return resource;
    }

}
