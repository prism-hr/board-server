package hr.prism.board.value;

import com.stripe.model.Invoice;
import hr.prism.board.domain.Board;
import hr.prism.board.domain.Department;
import hr.prism.board.domain.ResourceTask;

import java.util.List;

public class DepartmentDashboard {

    private Department department;

    private List<ResourceTask> tasks;

    private List<Board> boards;

    private Statistics memberStatistics;

    private List<OrganizationStatistics> organizationStatistics;

    private PostStatistics postStatistics;

    private List<Invoice> invoices;

    public Department getDepartment() {
        return department;
    }

    public DepartmentDashboard setDepartment(Department department) {
        this.department = department;
        return this;
    }

    public List<ResourceTask> getTasks() {
        return tasks;
    }

    public DepartmentDashboard setTasks(List<ResourceTask> tasks) {
        this.tasks = tasks;
        return this;
    }

    public List<Board> getBoards() {
        return boards;
    }

    public DepartmentDashboard setBoards(List<Board> boards) {
        this.boards = boards;
        return this;
    }

    public Statistics getMemberStatistics() {
        return memberStatistics;
    }

    public DepartmentDashboard setMemberStatistics(Statistics memberStatistics) {
        this.memberStatistics = memberStatistics;
        return this;
    }

    public List<OrganizationStatistics> getOrganizationStatistics() {
        return organizationStatistics;
    }

    public DepartmentDashboard setOrganizationStatistics(List<OrganizationStatistics> organizationStatistics) {
        this.organizationStatistics = organizationStatistics;
        return this;
    }

    public PostStatistics getPostStatistics() {
        return postStatistics;
    }

    public DepartmentDashboard setPostStatistics(PostStatistics postStatistics) {
        this.postStatistics = postStatistics;
        return this;
    }

    public List<Invoice> getInvoices() {
        return invoices;
    }

    public DepartmentDashboard setInvoices(List<Invoice> invoices) {
        this.invoices = invoices;
        return this;
    }

}
