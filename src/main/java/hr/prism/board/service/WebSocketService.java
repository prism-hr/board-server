package hr.prism.board.service;

import com.google.common.collect.ImmutableList;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.inject.Inject;

import hr.prism.board.representation.ActivityRepresentation;

@Service
@SuppressWarnings("SpringAutowiredFieldsWarningInspection")
public class WebSocketService {

    private volatile Set<Long> userIds = new LinkedHashSet<>();

    @Inject
    private SimpMessagingTemplate simpMessagingTemplate;

    @Async
    @EventListener
    public synchronized void handleSessionConnectedEvent(SessionConnectedEvent sessionConnectedEvent) {
        userIds.add(Long.parseLong(sessionConnectedEvent.getUser().getName()));
    }

    @Async
    @EventListener
    public synchronized void handleSessionDisconnectedEvent(SessionDisconnectEvent sessionDisconnectEvent) {
        userIds.remove(Long.parseLong(sessionDisconnectEvent.getUser().getName()));
    }

    public synchronized List<Long> getUserIds() {
        return ImmutableList.copyOf(userIds);
    }

    public void sendActivities(Long userId, List<ActivityRepresentation> activities) {
        simpMessagingTemplate.convertAndSendToUser(Objects.toString(userId), "/activities", activities);
    }

}

