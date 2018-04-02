package hr.prism.board.representation;

import com.stripe.model.Invoice;

import java.time.LocalDateTime;
import java.util.List;

public class DepartmentDashboardRepresentation {

    private List<ResourceTaskRepresentation> tasks;

    private List<BoardRepresentation> boards;

    private StatisticsRepresentation memberStatistics;

    private List<OrganizationRepresentation> organizations;

    private PostStatisticsRepresentation postStatistics;

    private List<Invoice> invoices;

    public List<ResourceTaskRepresentation> getTasks() {
        return tasks;
    }

    public DepartmentDashboardRepresentation setTasks(List<ResourceTaskRepresentation> tasks) {
        this.tasks = tasks;
        return this;
    }

    public List<BoardRepresentation> getBoards() {
        return boards;
    }

    public DepartmentDashboardRepresentation setBoards(List<BoardRepresentation> boards) {
        this.boards = boards;
        return this;
    }

    public StatisticsRepresentation getMemberStatistics() {
        return memberStatistics;
    }

    public DepartmentDashboardRepresentation setMemberStatistics(StatisticsRepresentation memberStatistics) {
        this.memberStatistics = memberStatistics;
        return this;
    }

    public List<OrganizationRepresentation> getOrganizations() {
        return organizations;
    }

    public DepartmentDashboardRepresentation setOrganizations(List<OrganizationRepresentation> organizations) {
        this.organizations = organizations;
        return this;
    }

    public PostStatisticsRepresentation getPostStatistics() {
        return postStatistics;
    }

    public DepartmentDashboardRepresentation setPostStatistics(PostStatisticsRepresentation postStatistics) {
        this.postStatistics = postStatistics;
        return this;
    }

    public List<Invoice> getInvoices() {
        return invoices;
    }

    public DepartmentDashboardRepresentation setInvoices(List<Invoice> invoices) {
        this.invoices = invoices;
        return this;
    }

    @SuppressWarnings("unchecked")
    public static class StatisticsRepresentation<T extends StatisticsRepresentation> {

        private Long countLive;

        private Long countAllTime;

        private LocalDateTime mostRecent;

        public Long getCountLive() {
            return countLive;
        }

        public T setCountLive(Long countLive) {
            this.countLive = countLive;
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

    }

    public static class PostStatisticsRepresentation extends StatisticsRepresentation<PostStatisticsRepresentation> {

        private Long countThisYear;

        private Long viewCountThisYear;

        private Long referralCountThisYear;

        private Long responseCountThisYear;

        private Long viewCountAllTime;

        private Long referralCountAllTime;

        private Long responseCountAllTime;

        private LocalDateTime mostRecentView;

        private LocalDateTime mostRecentReferral;

        private LocalDateTime mostRecentResponse;

        public Long getCountThisYear() {
            return countThisYear;
        }

        public PostStatisticsRepresentation setCountThisYear(Long countThisYear) {
            this.countThisYear = countThisYear;
            return this;
        }

        public Long getViewCountThisYear() {
            return viewCountThisYear;
        }

        public PostStatisticsRepresentation setViewCountThisYear(Long viewCountThisYear) {
            this.viewCountThisYear = viewCountThisYear;
            return this;
        }

        public Long getReferralCountThisYear() {
            return referralCountThisYear;
        }

        public PostStatisticsRepresentation setReferralCountThisYear(Long referralCountThisYear) {
            this.referralCountThisYear = referralCountThisYear;
            return this;
        }

        public Long getResponseCountThisYear() {
            return responseCountThisYear;
        }

        public PostStatisticsRepresentation setResponseCountThisYear(Long responseCountThisYear) {
            this.responseCountThisYear = responseCountThisYear;
            return this;
        }

        public Long getViewCountAllTime() {
            return viewCountAllTime;
        }

        public PostStatisticsRepresentation setViewCountAllTime(Long viewCountAllTime) {
            this.viewCountAllTime = viewCountAllTime;
            return this;
        }

        public Long getReferralCountAllTime() {
            return referralCountAllTime;
        }

        public PostStatisticsRepresentation setReferralCountAllTime(Long referralCountAllTime) {
            this.referralCountAllTime = referralCountAllTime;
            return this;
        }

        public Long getResponseCountAllTime() {
            return responseCountAllTime;
        }

        public PostStatisticsRepresentation setResponseCountAllTime(Long responseCountAllTime) {
            this.responseCountAllTime = responseCountAllTime;
            return this;
        }

        public LocalDateTime getMostRecentView() {
            return mostRecentView;
        }

        public PostStatisticsRepresentation setMostRecentView(LocalDateTime mostRecentView) {
            this.mostRecentView = mostRecentView;
            return this;
        }

        public LocalDateTime getMostRecentReferral() {
            return mostRecentReferral;
        }

        public PostStatisticsRepresentation setMostRecentReferral(LocalDateTime mostRecentReferral) {
            this.mostRecentReferral = mostRecentReferral;
            return this;
        }

        public LocalDateTime getMostRecentResponse() {
            return mostRecentResponse;
        }

        public PostStatisticsRepresentation setMostRecentResponse(LocalDateTime mostRecentResponse) {
            this.mostRecentResponse = mostRecentResponse;
            return this;
        }

    }

    public static class OrganizationRepresentation {

        private String organizationName;

        private String organizationLogo;

        private Long postCount;

        private LocalDateTime mostRecentPost;

        private Long postViewCount;

        private Long postReferralCount;

        private Long postResponseCount;

        public String getOrganizationName() {
            return organizationName;
        }

        public OrganizationRepresentation setOrganizationName(String organizationName) {
            this.organizationName = organizationName;
            return this;
        }

        public String getOrganizationLogo() {
            return organizationLogo;
        }

        public OrganizationRepresentation setOrganizationLogo(String organizationLogo) {
            this.organizationLogo = organizationLogo;
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

}
