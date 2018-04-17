package hr.prism.board.representation;

import com.stripe.model.Invoice;

import java.util.List;

public class DepartmentDashboardRepresentation {

    private List<ResourceTaskRepresentation> tasks;

    private List<BoardRepresentation> boards;

    private StatisticsRepresentation memberStatistics;

    private List<OrganizationStatisticsRepresentation> organizationStatistics;

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

    public List<OrganizationStatisticsRepresentation> getOrganizationStatistics() {
        return organizationStatistics;
    }

    public DepartmentDashboardRepresentation setOrganizationStatistics(
        List<OrganizationStatisticsRepresentation> organizationStatistics) {
        this.organizationStatistics = organizationStatistics;
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

}
