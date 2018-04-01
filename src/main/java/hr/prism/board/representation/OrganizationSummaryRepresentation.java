package hr.prism.board.representation;

import java.time.LocalDateTime;

public class OrganizationSummaryRepresentation {

    private String organizationName;

    private String organizationLogo;

    private Long count;

    private LocalDateTime mostRecent;

    private Long viewCount;

    private Long referralCount;

    private Long responseCount;

    public OrganizationSummaryRepresentation(String organizationName, String organizationLogo, Long count,
                                             LocalDateTime mostRecent, Long viewCount, Long referralCount,
                                             Long responseCount) {
        this.organizationName = organizationName;
        this.organizationLogo = organizationLogo;
        this.count = count;
        this.mostRecent = mostRecent;
        this.viewCount = viewCount;
        this.referralCount = referralCount;
        this.responseCount = responseCount;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public String getOrganizationLogo() {
        return organizationLogo;
    }

    public Long getCount() {
        return count;
    }

    public LocalDateTime getMostRecent() {
        return mostRecent;
    }

    public Long getViewCount() {
        return viewCount;
    }

    public Long getReferralCount() {
        return referralCount;
    }

    public Long getResponseCount() {
        return responseCount;
    }

}
