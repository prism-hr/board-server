package hr.prism.board.value;

import java.time.LocalDateTime;

public class Organization {

    private String name;

    private String logo;

    private Long postCount;

    private LocalDateTime mostRecentPost;

    private Long postViewCount;

    private Long postReferralCount;

    private Long postResponseCount;

    public Organization(String name, String logo) {
        this.name = name;
        this.logo = logo;
    }

    public Organization(String name, String logo, Long postCount, LocalDateTime mostRecentPost, Long postViewCount,
                        Long postReferralCount, Long postResponseCount) {
        this.name = name;
        this.logo = logo;
        this.postCount = postCount;
        this.mostRecentPost = mostRecentPost;
        this.postViewCount = postViewCount;
        this.postReferralCount = postReferralCount;
        this.postResponseCount = postResponseCount;
    }

    public String getName() {
        return name;
    }

    public String getLogo() {
        return logo;
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
