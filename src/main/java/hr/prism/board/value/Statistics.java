package hr.prism.board.value;

import java.time.LocalDateTime;

public class Statistics {

    private Long countLive;

    private Long countThisYear;

    private Long countAllTime;

    private LocalDateTime mostRecent;

    public Statistics(Long countLive, Long countThisYear, Long countAllTime, LocalDateTime mostRecent) {
        this.countLive = countLive;
        this.countThisYear = countThisYear;
        this.countAllTime = countAllTime;
        this.mostRecent = mostRecent;
    }

    public Long getCountLive() {
        return countLive;
    }

    public Long getCountThisYear() {
        return countThisYear;
    }

    public Long getCountAllTime() {
        return countAllTime;
    }

    public LocalDateTime getMostRecent() {
        return mostRecent;
    }

}
