package hr.prism.board.service;

import hr.prism.board.domain.User;
import hr.prism.board.enums.Notification;
import hr.prism.board.notification.BoardAttachments;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.fail;

@Service
public class TestNotificationService {

    public void verify(NotificationInstance... expectedNotificationInstances) {
        fail("Replace this shit with proper assertions against the pusher mock");
//        Assert.assertEquals(expectedNotificationInstances.length, instances.size());
//        for (NotificationInstance expectedNotificationInstance : expectedNotificationInstances) {
//            NotificationInstance actualNotificationInstance = instances.remove(0);
//            Assert.assertEquals(expectedNotificationInstance.getNotification(), actualNotificationInstance.getNotification());
//            Assert.assertEquals(expectedNotificationInstance.getRecipient(), actualNotificationInstance.getRecipient());
//
//            Map<String, String> actualParameters = actualNotificationInstance.getProperties();
//            Map<String, String> expectedParameters = expectedNotificationInstance.getProperties();
//            for (String expectedParameterKey : expectedParameters.keySet()) {
//                Assert.assertEquals(expectedParameters.get(expectedParameterKey), actualParameters.remove(expectedParameterKey));
//            }
//
//            // Any remaining parameters not defined in the expectations should have null values
//            actualParameters.keySet().forEach(actualParameterKey -> Assert.assertNull(actualParameters.get(actualParameterKey)));
//
//            List<BoardAttachments> expectedAttachments = expectedNotificationInstance.getAttachments();
//            List<BoardAttachments> actualAttachments = actualNotificationInstance.getAttachments();
//
//            int expectedAttachmentsSize = expectedAttachments.size();
//            Assert.assertEquals(expectedAttachmentsSize, actualAttachments.size());
//            for (int i = 0; i < expectedAttachmentsSize; i++) {
//                Attachments expectedAttachment = expectedAttachments.get(i);
//                Attachments actualAttachment = actualAttachments.get(i);
//
//                Assert.assertEquals(expectedAttachment.getContent(), actualAttachment.getContent());
//                Assert.assertEquals(expectedAttachment.getContentId(), actualAttachment.getContentId());
//                Assert.assertEquals(expectedAttachment.getDisposition(), actualAttachment.getDisposition());
//                Assert.assertEquals(expectedAttachment.getFilename(), actualAttachment.getFilename());
//                Assert.assertEquals(expectedAttachment.getType(), actualAttachment.getType());
//            }
//        }
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
