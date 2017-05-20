package hr.prism.board.service;

import org.junit.Assert;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Service
public class TestNotificationService extends NotificationService {
    
    private List<Notification> sent = new LinkedList<>();
    
    public void verify(Notification expectedNotification) {
        Assert.assertEquals(1, sent.size());
        
        Notification actualNotification = sent.remove(0);
        Assert.assertEquals(expectedNotification.getTemplate(), actualNotification.getTemplate());
        Assert.assertEquals(expectedNotification.getSender(), actualNotification.getSender());
        Assert.assertEquals(expectedNotification.getRecipient(), actualNotification.getRecipient());
        
        Map<String, String> actualParameters = actualNotification.getParameters();
        Map<String, String> expectedParameters = expectedNotification.getParameters();
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
    
    @Override
    public void send(Notification notification) {
        super.send(notification);
        sent.add(notification);
    }
    
}
