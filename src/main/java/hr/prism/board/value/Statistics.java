package hr.prism.board.value;

import java.time.LocalDateTime;

public class Statistics {

    private Long count;

    private Long countAllTime;

    private LocalDateTime mostRecent;

    public Statistics(Long count, Long countAllTime, LocalDateTime mostRecent) {
        this.count = count;
        this.countAllTime = countAllTime;
        this.mostRecent = mostRecent;
    }

    public Long getCount() {
        return count;
    }

    public Long getCountAllTime() {
        return countAllTime;
    }

    public LocalDateTime getMostRecent() {
        return mostRecent;
    }

}
