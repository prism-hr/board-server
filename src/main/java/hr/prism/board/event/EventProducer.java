package hr.prism.board.event;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

@Component
public class EventProducer {

    private final ApplicationContext applicationContext;

    @Inject
    public EventProducer(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Lazily fetches the application event publisher and publishes an event
     * <p>
     * This lets us avoid circular wiring problems when we want to mock the application event publisher
     *
     * @param events the events to be produced
     */
    public void produce(ApplicationEvent... events) {
        requireNonNull(events, "events cannot be null");
        ApplicationEventPublisher producer = applicationContext.getBean(ApplicationEventPublisher.class);
        Stream.of(events).forEach(producer::publishEvent);
    }

}
