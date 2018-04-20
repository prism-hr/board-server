package hr.prism.board.event;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

@Component
public class EventProducer {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Inject
    public EventProducer(@Lazy ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void produce(ApplicationEvent... events) {
        requireNonNull(events, "events cannot be null");
        Stream.of(events).forEach(applicationEventPublisher::publishEvent);
    }

}
