package hr.prism.board.service;

import org.junit.Assert;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Service
public class TestNotificationService extends NotificationService {

    private boolean recording = false;

    private List<NotificationInstance> sent = new LinkedList<>();

    public void record() {
        this.recording = true;
    }

    public void stop() {
        this.recording = false;
    }

    public void verify(NotificationInstance... expectedNotificationInstances) {
        Assert.assertEquals(1, expectedNotificationInstances.length);
        for (NotificationInstance expectedNotificationInstance : expectedNotificationInstances) {
            NotificationInstance actualNotificationInstance = sent.remove(0);
            Assert.assertEquals(expectedNotificationInstance.getTemplate(), actualNotificationInstance.getTemplate());
            Assert.assertEquals(expectedNotificationInstance.getSender(), actualNotificationInstance.getSender());
            Assert.assertEquals(expectedNotificationInstance.getRecipient(), actualNotificationInstance.getRecipient());

            Map<String, String> actualParameters = actualNotificationInstance.getParameters();
            Map<String, String> expectedParameters = expectedNotificationInstance.getParameters();
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
    public void sendNotification(NotificationInstance notificationInstance) {
        super.sendNotification(notificationInstance);
        if (recording) {
            sent.add(notificationInstance);
        }
    }

}
