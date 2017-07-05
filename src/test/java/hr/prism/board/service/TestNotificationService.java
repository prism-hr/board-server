package hr.prism.board.service;

import hr.prism.board.domain.User;
import hr.prism.board.enums.Notification;
import org.junit.Assert;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Service
public class TestNotificationService extends NotificationService {

    private boolean recording = false;

    private List<NotificationInstance> instances = new LinkedList<>();

    public void record() {
        this.recording = true;
    }

    public void clear() {
        this.recording = false;
    }

    public void verify(NotificationInstance... expectedNotificationInstances) {
        Assert.assertEquals(instances.size(), expectedNotificationInstances.length);
        for (NotificationInstance expectedNotificationInstance : expectedNotificationInstances) {
            NotificationInstance actualNotificationInstance = instances.remove(0);
            Assert.assertEquals(expectedNotificationInstance.getNotification(), actualNotificationInstance.getNotification());
            Assert.assertEquals(expectedNotificationInstance.getRecipient(), actualNotificationInstance.getRecipient());

            Map<String, String> actualParameters = actualNotificationInstance.getProperties();
            Map<String, String> expectedParameters = expectedNotificationInstance.getProperties();
            for (String expectedParameterKey : expectedParameters.keySet()) {
                String expectedParameterValue = expectedParameters.get(expectedParameterKey);
                if (expectedParameterValue.equals("defined")) {
                    // We can't easily compare dynamic values (e.g. temporaryPassword), so assert not null
                    Assert.assertNotNull(actualParameters.remove(expectedParameterKey));
                } else {
                    // Parameters that are statically defined can be compared, assert equals
                    Assert.assertEquals(expectedParameterValue, actualParameters.remove(expectedParameterKey));
                }
            }

            // Any remaining parameters not defined in the expectations should have null values
            actualParameters.keySet().forEach(actualParameterKey -> Assert.assertNull(actualParameters.get(actualParameterKey)));
        }
    }

    @Override
    public Map<String, String> sendNotification(NotificationRequest request) {
        Map<String, String> properties = super.sendNotification(request);
        if (recording) {
            instances.add(new NotificationInstance(request.getNotification(), request.getRecipient(), properties));
        }

        return properties;
    }

    public static class NotificationInstance {

        private Notification notification;

        private User recipient;

        private Map<String, String> properties;

        public NotificationInstance(Notification notification, User recipient, Map<String, String> properties) {
            this.notification = notification;
            this.recipient = recipient;
            this.properties = properties;
        }

        public Notification getNotification() {
            return notification;
        }

        public User getRecipient() {
            return recipient;
        }

        public Map<String, String> getProperties() {
            return properties;
        }

    }

}
