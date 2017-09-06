package hr.prism.board.value;

import hr.prism.board.enums.Scope;

import java.time.LocalDateTime;

public class ResourceSummary extends Summary<Scope> {

    public ResourceSummary(Scope key, Long count, LocalDateTime lastTimestamp) {
        super(key, count, lastTimestamp);
    }

}
