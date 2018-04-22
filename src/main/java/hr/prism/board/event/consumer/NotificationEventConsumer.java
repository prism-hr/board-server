package hr.prism.board.event.consumer;

import com.google.common.collect.HashMultimap;
import hr.prism.board.domain.Post;
import hr.prism.board.domain.Resource;
import hr.prism.board.domain.User;
import hr.prism.board.enums.Action;
import hr.prism.board.event.NotificationEvent;
import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.BoardNotificationException;
import hr.prism.board.notification.BoardAttachments;
import hr.prism.board.service.NotificationService;
import hr.prism.board.service.NotificationService.NotificationRequest;
import hr.prism.board.service.ResourceEventService;
import hr.prism.board.service.ResourceService;
import hr.prism.board.value.UserNotification;
import hr.prism.board.workflow.Notification;
import hr.prism.board.workflow.Notification.Attachment;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import static hr.prism.board.exception.ExceptionCode.CONNECTION_ERROR;
import static java.util.Base64.getEncoder;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.io.IOUtils.toByteArray;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class NotificationEventConsumer {

    private static Logger LOGGER = getLogger(NotificationEventConsumer.class);

    @Inject
    private final ResourceService resourceService;

    @Inject
    private final NotificationService notificationService;

    @Inject
    private final ResourceEventService resourceEventService;

    @Inject
    private final ApplicationContext applicationContext;

    public NotificationEventConsumer(ResourceService resourceService, NotificationService notificationService,
                                     ResourceEventService resourceEventService, ApplicationContext applicationContext) {
        this.resourceService = resourceService;
        this.notificationService = notificationService;
        this.resourceEventService = resourceEventService;
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
                                mapAttachments(notification.getAttachments())));
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

        Resource resource = resourceService.findOne(resourceId);
        if (resource == null) {
            return null;
        }

        Long resourceEventId = notificationEvent.getResourceEventId();
        if (resourceEventId != null) {
            ((Post) resource).setResponse(resourceEventService.getById(resourceEventId));
        }

        return resource;
    }

    private List<BoardAttachments> mapAttachments(List<Attachment> attachments) {
        return attachments.isEmpty() ? emptyList() : attachments.stream().map(this::mapAttachment).collect(toList());
    }

    private BoardAttachments mapAttachment(Attachment attachment) {
        try {
            String urlPath = attachment.getUrl();
            URL url = new URL(urlPath);
            URLConnection connection = url.openConnection();
            try (InputStream inputStream = connection.getInputStream()) {
                BoardAttachments attachments = new BoardAttachments();
                attachments.setContent(getEncoder().encodeToString(toByteArray(inputStream)));
                attachments.setType(connection.getContentType());
                attachments.setFilename(attachment.getName());
                attachments.setDisposition("attachment");
                attachments.setContentId(attachment.getLabel());
                attachment.setUrl(urlPath);
                return attachments;
            } catch (IOException e) {
                throw new BoardException(CONNECTION_ERROR, "Could not retrieve attachment data");
            }
        } catch (IOException e) {
            throw new BoardException(CONNECTION_ERROR, "Could not access attachment data");
        }
    }

}
