package hr.prism.board.representation;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.time.LocalDateTime;

public class PostStatisticsRepresentation extends StatisticsRepresentation<PostStatisticsRepresentation> {

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

    public Long getViewCountLive() {
        return viewCountLive;
    }

    public PostStatisticsRepresentation setViewCountLive(Long viewCountLive) {
        this.viewCountLive = viewCountLive;
        return this;
    }

    public Long getViewCountThisYear() {
        return viewCountThisYear;
    }

    public PostStatisticsRepresentation setViewCountThisYear(Long viewCountThisYear) {
        this.viewCountThisYear = viewCountThisYear;
        return this;
    }

    public Long getViewCountAllTime() {
        return viewCountAllTime;
    }

    public PostStatisticsRepresentation setViewCountAllTime(Long viewCountAllTime) {
        this.viewCountAllTime = viewCountAllTime;
        return this;
    }

    public LocalDateTime getMostRecentView() {
        return mostRecentView;
    }

    public PostStatisticsRepresentation setMostRecentView(LocalDateTime mostRecentView) {
        this.mostRecentView = mostRecentView;
        return this;
    }

    public Long getReferralCountLive() {
        return referralCountLive;
    }

    public PostStatisticsRepresentation setReferralCountLive(Long referralCountLive) {
        this.referralCountLive = referralCountLive;
        return this;
    }

    public Long getReferralCountThisYear() {
        return referralCountThisYear;
    }

    public PostStatisticsRepresentation setReferralCountThisYear(Long referralCountThisYear) {
        this.referralCountThisYear = referralCountThisYear;
        return this;
    }

    public Long getReferralCountAllTime() {
        return referralCountAllTime;
    }

    public PostStatisticsRepresentation setReferralCountAllTime(Long referralCountAllTime) {
        this.referralCountAllTime = referralCountAllTime;
        return this;
    }

    public LocalDateTime getMostRecentReferral() {
        return mostRecentReferral;
    }

    public PostStatisticsRepresentation setMostRecentReferral(LocalDateTime mostRecentReferral) {
        this.mostRecentReferral = mostRecentReferral;
        return this;
    }

    public Long getResponseCountLive() {
        return responseCountLive;
    }

    public PostStatisticsRepresentation setResponseCountLive(Long responseCountLive) {
        this.responseCountLive = responseCountLive;
        return this;
    }

    public Long getResponseCountThisYear() {
        return responseCountThisYear;
    }

    public PostStatisticsRepresentation setResponseCountThisYear(Long responseCountThisYear) {
        this.responseCountThisYear = responseCountThisYear;
        return this;
    }

    public Long getResponseCountAllTime() {
        return responseCountAllTime;
    }

    public PostStatisticsRepresentation setResponseCountAllTime(Long responseCountAllTime) {
        this.responseCountAllTime = responseCountAllTime;
        return this;
    }

    public LocalDateTime getMostRecentResponse() {
        return mostRecentResponse;
    }

    public PostStatisticsRepresentation setMostRecentResponse(LocalDateTime mostRecentResponse) {
        this.mostRecentResponse = mostRecentResponse;
        return this;
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(
            17, 37, this, false, StatisticsRepresentation.class);
    }

    @Override
    public boolean equals(Object other) {
        return EqualsBuilder.reflectionEquals(this, other, false, StatisticsRepresentation.class);
    }

}
