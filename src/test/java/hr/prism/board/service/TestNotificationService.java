package hr.prism.board.service;

import com.sendgrid.Attachments;
import com.sendgrid.SendGrid;
import hr.prism.board.domain.User;
import hr.prism.board.enums.Notification;
import hr.prism.board.notification.BoardAttachments;
import org.junit.Assert;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TestNotificationService extends NotificationService {

    private boolean recording = false;

    private List<NotificationInstance> instances = new LinkedList<>();

    public TestNotificationService(boolean mailOn, String senderEmail, TestEmailService testEmailService,
                                   SendGrid sendGrid, ApplicationContext applicationContext) {
        super(mailOn, senderEmail, testEmailService, sendGrid, applicationContext);
    }

    public void record() {
        this.recording = true;
        this.instances.clear();
    }

    public void stop() {
        this.recording = false;
    }

    public void verify(NotificationInstance... expectedNotificationInstances) {
        Assert.assertEquals(expectedNotificationInstances.length, instances.size());
        for (NotificationInstance expectedNotificationInstance : expectedNotificationInstances) {
            NotificationInstance actualNotificationInstance = instances.remove(0);
            Assert.assertEquals(expectedNotificationInstance.getNotification(), actualNotificationInstance.getNotification());
            Assert.assertEquals(expectedNotificationInstance.getRecipient(), actualNotificationInstance.getRecipient());

            Map<String, String> actualParameters = actualNotificationInstance.getProperties();
            Map<String, String> expectedParameters = expectedNotificationInstance.getProperties();
            for (String expectedParameterKey : expectedParameters.keySet()) {
                Assert.assertEquals(expectedParameters.get(expectedParameterKey), actualParameters.remove(expectedParameterKey));
            }

            // Any remaining parameters not defined in the expectations should have null values
            actualParameters.keySet().forEach(actualParameterKey -> Assert.assertNull(actualParameters.get(actualParameterKey)));

            List<BoardAttachments> expectedAttachments = expectedNotificationInstance.getAttachments();
            List<BoardAttachments> actualAttachments = actualNotificationInstance.getAttachments();

            int expectedAttachmentsSize = expectedAttachments.size();
            Assert.assertEquals(expectedAttachmentsSize, actualAttachments.size());
            for (int i = 0; i < expectedAttachmentsSize; i++) {
                Attachments expectedAttachment = expectedAttachments.get(i);
                Attachments actualAttachment = actualAttachments.get(i);

                Assert.assertEquals(expectedAttachment.getContent(), actualAttachment.getContent());
                Assert.assertEquals(expectedAttachment.getContentId(), actualAttachment.getContentId());
                Assert.assertEquals(expectedAttachment.getDisposition(), actualAttachment.getDisposition());
                Assert.assertEquals(expectedAttachment.getFilename(), actualAttachment.getFilename());
                Assert.assertEquals(expectedAttachment.getType(), actualAttachment.getType());
            }
        }
    }

    @Override
    public Map<String, String> sendNotification(NotificationRequest request) {
        Map<String, String> properties = super.sendNotification(request);
        if (recording) {
            instances.add(new NotificationInstance(request.getNotification(), request.getRecipient(), properties, request.getAttachments()));
        }

        return properties;
    }

    public static class NotificationInstance {

        private Notification notification;

        private User recipient;

        private Map<String, String> properties;

        private List<BoardAttachments> attachments = new ArrayList<>();

        public NotificationInstance(Notification notification, User recipient, Map<String, String> properties) {
            this.notification = notification;
            this.recipient = recipient;
            this.properties = properties;
        }

        public NotificationInstance(Notification notification, User recipient, Map<String, String> properties, List<BoardAttachments> attachments) {
            this.notification = notification;
            this.recipient = recipient;
            this.properties = properties;
            this.attachments = attachments;
        }

        Notification getNotification() {
            return notification;
        }

        User getRecipient() {
            return recipient;
        }

        Map<String, String> getProperties() {
            return properties;
        }

        List<BoardAttachments> getAttachments() {
            return attachments;
        }

    }

}
