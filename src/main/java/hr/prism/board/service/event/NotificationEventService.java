package hr.prism.board.service.event;

import com.google.common.collect.HashMultimap;
import com.sendgrid.Attachments;
import hr.prism.board.domain.Post;
import hr.prism.board.domain.Resource;
import hr.prism.board.domain.User;
import hr.prism.board.enums.Action;
import hr.prism.board.event.NotificationEvent;
import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.service.NotificationService;
import hr.prism.board.service.ResourceEventService;
import hr.prism.board.service.ResourceService;
import hr.prism.board.value.UserNotification;
import hr.prism.board.workflow.Notification;
import org.apache.commons.io.IOUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class NotificationEventService {

    @Inject
    private ResourceService resourceService;

    @Inject
    private NotificationService notificationService;

    @Inject
    private ResourceEventService resourceEventService;

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

    public void publishEvent(Object source, Long resourceId, Long resourceEventId, List<Notification> notifications) {
        applicationEventPublisher.publishEvent(new NotificationEvent(source, resourceId, resourceEventId, notifications));
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

        Long resourceEventId = notificationEvent.getResourceEventId();
        if (resourceEventId != null) {
            ((Post) resource).setResponse(resourceEventService.findOne(resourceEventId));
        }

        Action action = notificationEvent.getAction();
        List<Notification> notifications = notificationEvent.getNotifications();
        HashMultimap<User, hr.prism.board.enums.Notification> sent = HashMultimap.create();
        for (Notification notification : notifications) {
            hr.prism.board.enums.Notification template = notification.getNotification();

            List<UserNotification> recipients = applicationContext.getBean(template.getRecipients()).list(resource, notification);
            for (UserNotification recipient : recipients) {
                User user = recipient.getUser();
                if (!sent.containsEntry(user, template)) {
                    notificationService.sendNotification(
                        new NotificationService.NotificationRequest(template, user, recipient.getInvitation(), resource, action, mapAttachments(notification.getAttachments())));
                    sent.put(user, template);
                }
            }
        }
    }

    private List<Attachments> mapAttachments(List<Notification.Attachment> attachments) {
        if (attachments.isEmpty()) {
            return Collections.emptyList();
        }

        return attachments.stream().map(this::mapAttachment).collect(Collectors.toList());
    }

    private Attachments mapAttachment(Notification.Attachment attachment) {
        InputStream inputStream = null;

        try {
            URL url = new URL(attachment.getUrl());
            URLConnection connection = url.openConnection();
            inputStream = connection.getInputStream();

            Attachments attachments = new Attachments();
            attachments.setContent(Base64.getEncoder().encodeToString(IOUtils.toByteArray(inputStream)));
            attachments.setType(connection.getContentType());
            attachments.setFilename(attachment.getName());
            attachments.setDisposition("attachment");
            attachments.setContentId(attachment.getLabel());
            return attachments;
        } catch (IOException e) {
            throw new BoardException(ExceptionCode.FAILING_INTEGRATION, "Could not retrieve attachment data");
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

}
