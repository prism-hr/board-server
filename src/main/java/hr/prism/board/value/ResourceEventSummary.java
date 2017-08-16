package hr.prism.board.value;

import hr.prism.board.enums.ResourceEvent;

import java.time.LocalDateTime;

public class ResourceEventSummary extends Summary<ResourceEvent> {

    public ResourceEventSummary(ResourceEvent key, Long count, LocalDateTime lastTimestamp) {
        super(key, count, lastTimestamp);
    }

}
