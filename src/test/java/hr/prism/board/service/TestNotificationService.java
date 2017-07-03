package hr.prism.board.service;

import org.junit.Assert;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class TestNotificationService extends NotificationService {

    private boolean recording = false;

    private List<NotificationInstance> instances = new LinkedList<>();

    public void record() {
        this.recording = true;
    }

    public void stop() {
        this.recording = false;
    }

    public void verify(NotificationInstance... expectedNotificationInstances) {
        Assert.assertEquals(1, expectedNotificationInstances.length);
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
            instances.add(new NotificationInstance(request, properties));
        }

        return properties;
    }

    public static class NotificationInstance extends NotificationRequest {

        private Map<String, String> properties;

        public NotificationInstance(NotificationRequest request, Map<String, String> properties) {
            super(request.getNotification(), request.getRecipient(), request.getResource(), request.getAction(), null);
            this.properties = properties;
        }

        public Map<String, String> getProperties() {
            return properties;
        }

        @Override
        public int hashCode() {
            return Objects.hash(getNotification(), getRecipient(), getResource(), getAction(), properties);
        }

        @Override
        public boolean equals(Object object) {
            if (object == null || object.getClass() != getClass()) {
                return false;
            }

            NotificationInstance that = (NotificationInstance) object;
            return Objects.equals(getNotification(), that.getNotification()) && Objects.equals(getRecipient(), that.getRecipient())
                && Objects.equals(getResource(), that.getResource()) && Objects.equals(getAction(), that.getAction()) && Objects.equals(properties, that.getProperties());
        }

    }

}
