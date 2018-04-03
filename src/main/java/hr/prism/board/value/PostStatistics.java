package hr.prism.board.value;

import java.time.LocalDateTime;

public class PostStatistics extends Statistics {

    private Long viewCountLive;

    private Long viewCountThisYear;

    private Long viewCountAllTime;

    private LocalDateTime mostRecentView;

    private Long referralCountLive;

    private Long referralCountThisYear;

    private Long referralCountAllTime;

    private LocalDateTime mostRecentReferral;

    private Long responseCountLive;

    private Long responseCountThisYear;

    private Long responseCountAllTime;

    private LocalDateTime mostRecentResponse;

    public PostStatistics(Long countLive, Long countThisYear, Long countAllTime, LocalDateTime mostRecent, Long viewCountLive,
                          Long viewCountThisYear, Long viewCountAllTime, LocalDateTime mostRecentView, Long referralCountLive,
                          Long referralCountThisYear, Long referralCountAllTime, LocalDateTime mostRecentReferral,
                          Long responseCountLive, Long responseCountThisYear, Long responseCountAllTime, LocalDateTime mostRecentResponse) {
        super(countLive, countThisYear, countAllTime, mostRecent);
        this.viewCountLive = viewCountLive;
        this.viewCountThisYear = viewCountThisYear;
        this.viewCountAllTime = viewCountAllTime;
        this.mostRecentView = mostRecentView;
        this.referralCountLive = referralCountLive;
        this.referralCountThisYear = referralCountThisYear;
        this.referralCountAllTime = referralCountAllTime;
        this.mostRecentReferral = mostRecentReferral;
        this.responseCountLive = responseCountLive;
        this.responseCountThisYear = responseCountThisYear;
        this.responseCountAllTime = responseCountAllTime;
        this.mostRecentResponse = mostRecentResponse;
    }

    public Long getViewCountLive() {
        return viewCountLive;
    }

    public Long getViewCountThisYear() {
        return viewCountThisYear;
    }

    public Long getViewCountAllTime() {
        return viewCountAllTime;
    }

    public LocalDateTime getMostRecentView() {
        return mostRecentView;
    }

    public Long getReferralCountLive() {
        return referralCountLive;
    }

    public Long getReferralCountThisYear() {
        return referralCountThisYear;
    }

    public Long getReferralCountAllTime() {
        return referralCountAllTime;
    }

    public LocalDateTime getMostRecentReferral() {
        return mostRecentReferral;
    }

    public Long getResponseCountLive() {
        return responseCountLive;
    }

    public Long getResponseCountThisYear() {
        return responseCountThisYear;
    }

    public Long getResponseCountAllTime() {
        return responseCountAllTime;
    }

    public LocalDateTime getMostRecentResponse() {
        return mostRecentResponse;
    }

}
