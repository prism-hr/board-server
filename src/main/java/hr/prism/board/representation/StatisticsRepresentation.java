package hr.prism.board.representation;

import java.time.LocalDateTime;

import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;

public class StatisticsRepresentation<T extends StatisticsRepresentation> {

    private Long countLive;

    private Long countThisYear;

    private Long countAllTime;

    private LocalDateTime mostRecent;

    public Long getCountLive() {
        return countLive;
    }

    @SuppressWarnings("unchecked")
    public T setCountLive(Long countLive) {
        this.countLive = countLive;
        return (T) this;
    }

    public Long getCountThisYear() {
        return countThisYear;
    }

    @SuppressWarnings("unchecked")
    public T setCountThisYear(Long countThisYear) {
        this.countThisYear = countThisYear;
        return (T) this;
    }

    public Long getCountAllTime() {
        return countAllTime;
    }

    @SuppressWarnings("unchecked")
    public T setCountAllTime(Long countAllTime) {
        this.countAllTime = countAllTime;
        return (T) this;
    }

    public LocalDateTime getMostRecent() {
        return mostRecent;
    }

    @SuppressWarnings("unchecked")
    public T setMostRecent(LocalDateTime mostRecent) {
        this.mostRecent = mostRecent;
        return (T) this;
    }

    @Override
    public int hashCode() {
        return reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object other) {
        return reflectionEquals(this, other);
    }

}
