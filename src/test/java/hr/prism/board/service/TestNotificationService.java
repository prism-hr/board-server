package hr.prism.board.service;

import hr.prism.board.domain.User;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Service
public class TestNotificationService extends NotificationService {
    
    private List<Pair<String, String>> sent = new LinkedList<>();
    
    public void verify(String expectedSubject, String expectedContent) {
        Assert.assertEquals(1, sent.size());
        Assert.assertEquals(sent.get(0), Pair.of(expectedSubject, expectedContent));
        sent.clear();
    }
    
    @Override
    public Pair<String, String> send(User user, String notification, Map<String, String> customParameters) {
        Pair<String, String> mail = super.send(user, notification, customParameters);
        sent.add(mail);
        return mail;
    }
    
}
