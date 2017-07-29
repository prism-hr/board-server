package hr.prism.board.service.event;

import com.google.common.collect.HashMultimap;
import hr.prism.board.domain.Resource;
import hr.prism.board.domain.User;
import hr.prism.board.enums.Action;
import hr.prism.board.event.NotificationEvent;
import hr.prism.board.service.NotificationService;
import hr.prism.board.service.ResourceService;
import hr.prism.board.workflow.Notification;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class NotificationEventService {

    @Inject
    private ResourceService resourceService;

    @Inject
    private NotificationService notificationService;

    @Inject
    private ApplicationEventPublisher applicationEventPublisher;

    @Inject
    private ApplicationContext applicationContext;

    public void publishEvent(Object source, List<Notification> notifications) {
        applicationEventPublisher.publishEvent(new NotificationEvent(source, notifications));
    }

    public void publishEvent(Object source, Long resourceId, List<Notification> notifications) {
        applicationEventPublisher.publishEvent(new NotificationEvent(source, resourceId, notifications));
    }

    public void publishEvent(Object source, Long resourceId, Action action, List<Notification> notifications) {
        applicationEventPublisher.publishEvent(new NotificationEvent(source, resourceId, action, notifications));
    }

    @Async
    @TransactionalEventListener
    public void sendNotificationsAsync(NotificationEvent notificationEvent) {
        sendNotifications(notificationEvent);
    }

    protected void sendNotifications(NotificationEvent notificationEvent) {
        Resource resource = null;
        Long resourceId = notificationEvent.getResourceId();
        if (resourceId != null) {
            resource = resourceService.findOne(resourceId);
        }

        Action action = notificationEvent.getAction();
        List<Notification> notifications = notificationEvent.getNotifications();
        HashMultimap<User, hr.prism.board.enums.Notification> sent = HashMultimap.create();
        for (Notification notification : notifications) {
            hr.prism.board.enums.Notification template = notification.getNotification();

            List<User> recipients = applicationContext.getBean(template.getRecipients()).list(resource, notification);
            for (User recipient : recipients) {
                if (!sent.containsEntry(recipient, template)) {
                    notificationService.sendNotification(new NotificationService.NotificationRequest(template, recipient, resource, action));
                    sent.put(recipient, template);
                }
            }
        }
    }

}
