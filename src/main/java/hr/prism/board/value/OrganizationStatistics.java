package hr.prism.board.value;

import java.time.LocalDateTime;

public class OrganizationStatistics extends OrganizationSearch {

    private Long postCount;

    private LocalDateTime mostRecentPost;

    private Long postViewCount;

    private Long postReferralCount;

    private Long postResponseCount;

    public OrganizationStatistics(Long id, String name, String logo, Long postCount, LocalDateTime mostRecentPost,
                                  Long postViewCount, Long postReferralCount, Long postResponseCount) {
        super(id, name, logo);
        this.postCount = postCount;
        this.mostRecentPost = mostRecentPost;
        this.postViewCount = postViewCount;
        this.postReferralCount = postReferralCount;
        this.postResponseCount = postResponseCount;
    }

    public Long getPostCount() {
        return postCount;
    }

    public LocalDateTime getMostRecentPost() {
        return mostRecentPost;
    }

    public Long getPostViewCount() {
        return postViewCount;
    }

    public Long getPostReferralCount() {
        return postReferralCount;
    }

    public Long getPostResponseCount() {
        return postResponseCount;
    }

}
