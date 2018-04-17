package hr.prism.board.representation;

import java.time.LocalDateTime;

public class OrganizationStatisticsRepresentation
    extends OrganizationRepresentation<OrganizationStatisticsRepresentation> {

    private Long postCount;

    private LocalDateTime mostRecentPost;

    private Long postViewCount;

    private Long postReferralCount;

    private Long postResponseCount;

    public Long getPostCount() {
        return postCount;
    }

    public OrganizationStatisticsRepresentation setPostCount(Long postCount) {
        this.postCount = postCount;
        return this;
    }

    public LocalDateTime getMostRecentPost() {
        return mostRecentPost;
    }

    public OrganizationStatisticsRepresentation setMostRecentPost(LocalDateTime mostRecentPost) {
        this.mostRecentPost = mostRecentPost;
        return this;
    }

    public Long getPostViewCount() {
        return postViewCount;
    }

    public OrganizationStatisticsRepresentation setPostViewCount(Long postViewCount) {
        this.postViewCount = postViewCount;
        return this;
    }

    public Long getPostReferralCount() {
        return postReferralCount;
    }

    public OrganizationStatisticsRepresentation setPostReferralCount(Long postReferralCount) {
        this.postReferralCount = postReferralCount;
        return this;
    }

    public Long getPostResponseCount() {
        return postResponseCount;
    }

    public OrganizationStatisticsRepresentation setPostResponseCount(Long postResponseCount) {
        this.postResponseCount = postResponseCount;
        return this;
    }

}
