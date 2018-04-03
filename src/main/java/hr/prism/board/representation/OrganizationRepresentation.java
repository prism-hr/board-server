package hr.prism.board.representation;

import java.time.LocalDateTime;

public class OrganizationRepresentation {

    private String name;

    private String logo;

    private Long postCount;

    private LocalDateTime mostRecentPost;

    private Long postViewCount;

    private Long postReferralCount;

    private Long postResponseCount;

    public String getName() {
        return name;
    }

    public OrganizationRepresentation setName(String name) {
        this.name = name;
        return this;
    }

    public String getLogo() {
        return logo;
    }

    public OrganizationRepresentation setLogo(String logo) {
        this.logo = logo;
        return this;
    }

    public Long getPostCount() {
        return postCount;
    }

    public OrganizationRepresentation setPostCount(Long postCount) {
        this.postCount = postCount;
        return this;
    }

    public LocalDateTime getMostRecentPost() {
        return mostRecentPost;
    }

    public OrganizationRepresentation setMostRecentPost(LocalDateTime mostRecentPost) {
        this.mostRecentPost = mostRecentPost;
        return this;
    }

    public Long getPostViewCount() {
        return postViewCount;
    }

    public OrganizationRepresentation setPostViewCount(Long postViewCount) {
        this.postViewCount = postViewCount;
        return this;
    }

    public Long getPostReferralCount() {
        return postReferralCount;
    }

    public OrganizationRepresentation setPostReferralCount(Long postReferralCount) {
        this.postReferralCount = postReferralCount;
        return this;
    }

    public Long getPostResponseCount() {
        return postResponseCount;
    }

    public OrganizationRepresentation setPostResponseCount(Long postResponseCount) {
        this.postResponseCount = postResponseCount;
        return this;
    }

}
