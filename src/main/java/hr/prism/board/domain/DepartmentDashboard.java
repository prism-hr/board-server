package hr.prism.board.domain;

import com.stripe.model.Invoice;

import java.util.List;
import java.util.Set;

public class DepartmentDashboard {

    private Department department;

    private Set<ResourceTask> tasks;

    private List<Board> boards;

    private Object[] memberStatistics;

    private List<Object[]> organizations;

    private Object[] postStatistics;

    private List<Invoice> invoices;

    public Department getDepartment() {
        return department;
    }

    public DepartmentDashboard setDepartment(Department department) {
        this.department = department;
        return this;
    }

    public Set<ResourceTask> getTasks() {
        return tasks;
    }

    public DepartmentDashboard setTasks(Set<ResourceTask> tasks) {
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

    public Object[] getMemberStatistics() {
        return memberStatistics;
    }

    public DepartmentDashboard setMemberStatistics(Object[] memberStatistics) {
        this.memberStatistics = memberStatistics;
        return this;
    }

    public List<Object[]> getOrganizations() {
        return organizations;
    }

    public DepartmentDashboard setOrganizations(List<Object[]> organizations) {
        this.organizations = organizations;
        return this;
    }

    public Object[] getPostStatistics() {
        return postStatistics;
    }

    public DepartmentDashboard setPostStatistics(Object[] postStatistics) {
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
