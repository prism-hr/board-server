package hr.prism.board.value;

import hr.prism.board.enums.Scope;

import java.time.LocalDateTime;

public class ChildResourceSummary extends Summary<Scope> {

    public ChildResourceSummary(Scope key, Long count, LocalDateTime lastTimestamp) {
        super(key, count, lastTimestamp);
    }

}
