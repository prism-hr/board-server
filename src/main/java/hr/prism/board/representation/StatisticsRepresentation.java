package hr.prism.board.representation;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.time.LocalDateTime;

@SuppressWarnings("unchecked")
public class StatisticsRepresentation<T extends StatisticsRepresentation> {

    private Long countLive;

    private Long countThisYear;

    private Long countAllTime;

    private LocalDateTime mostRecent;

    public Long getCountLive() {
        return countLive;
    }

    public T setCountLive(Long countLive) {
        this.countLive = countLive;
        return (T) this;
    }

    public Long getCountThisYear() {
        return countThisYear;
    }

    public T setCountThisYear(Long countThisYear) {
        this.countThisYear = countThisYear;
        return (T) this;
    }

    public Long getCountAllTime() {
        return countAllTime;
    }

    public T setCountAllTime(Long countAllTime) {
        this.countAllTime = countAllTime;
        return (T) this;
    }

    public LocalDateTime getMostRecent() {
        return mostRecent;
    }

    public T setMostRecent(LocalDateTime mostRecent) {
        this.mostRecent = mostRecent;
        return (T) this;
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object other) {
        return EqualsBuilder.reflectionEquals(this, other);
    }

}
