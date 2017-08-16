package hr.prism.board.value;

import java.time.LocalDateTime;

public abstract class Summary<T extends Enum<T>> {

    private T key;

    private Long count;

    private LocalDateTime lastTimestamp;

    public Summary(T key, Long count, LocalDateTime lastTimestamp) {
        this.key = key;
        this.count = count;
        this.lastTimestamp = lastTimestamp;
    }

    public T getKey() {
        return key;
    }

    public Summary setKey(T key) {
        this.key = key;
        return this;
    }

    public Long getCount() {
        return count;
    }

    public Summary setCount(Long count) {
        this.count = count;
        return this;
    }

    public LocalDateTime getLastTimestamp() {
        return lastTimestamp;
    }

    public Summary setLastTimestamp(LocalDateTime lastTimestamp) {
        this.lastTimestamp = lastTimestamp;
        return this;
    }

}
