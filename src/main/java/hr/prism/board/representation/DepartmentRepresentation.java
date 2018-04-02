package hr.prism.board.representation;

import com.stripe.model.InvoiceCollection;
import hr.prism.board.enums.MemberCategory;

import java.time.LocalDateTime;
import java.util.List;

@SuppressWarnings("unused")
public class DepartmentRepresentation extends ResourceRepresentation<DepartmentRepresentation> {

    private String summary;

    private UniversityRepresentation university;

    private DocumentRepresentation documentLogo;

    private String handle;

    private List<MemberCategory> memberCategories;

    private String customerId;

    private Long postCount;

    private Long postCountAllTime;

    private LocalDateTime mostRecentPost;

    private Long memberCount;

    private Long memberCountAllTime;

    private LocalDateTime mostRecentMember;

    private List<ResourceTaskRepresentation> tasks;

    private List<BoardRepresentation> boards;

    private List<DepartmentDashboardRepresentation.OrganizationRepresentation> organizations;

    private InvoiceCollection invoices;

    public String getSummary() {
        return summary;
    }

    public DepartmentRepresentation setSummary(String summary) {
        this.summary = summary;
        return this;
    }

    public UniversityRepresentation getUniversity() {
        return university;
    }

    public DepartmentRepresentation setUniversity(UniversityRepresentation university) {
        this.university = university;
        return this;
    }

    public DocumentRepresentation getDocumentLogo() {
        return documentLogo;
    }

    public DepartmentRepresentation setDocumentLogo(DocumentRepresentation documentLogo) {
        this.documentLogo = documentLogo;
        return this;
    }

    public String getHandle() {
        return handle;
    }

    public DepartmentRepresentation setHandle(String handle) {
        this.handle = handle;
        return this;
    }

    public List<MemberCategory> getMemberCategories() {
        return memberCategories;
    }

    public DepartmentRepresentation setMemberCategories(List<MemberCategory> memberCategories) {
        this.memberCategories = memberCategories;
        return this;
    }

    public String getCustomerId() {
        return customerId;
    }

    public DepartmentRepresentation setCustomerId(String customerId) {
        this.customerId = customerId;
        return this;
    }

    public Long getPostCount() {
        return postCount;
    }

    public DepartmentRepresentation setPostCount(Long postCount) {
        this.postCount = postCount;
        return this;
    }

    public Long getPostCountAllTime() {
        return postCountAllTime;
    }

    public DepartmentRepresentation setPostCountAllTime(Long postCountAllTime) {
        this.postCountAllTime = postCountAllTime;
        return this;
    }

    public LocalDateTime getMostRecentPost() {
        return mostRecentPost;
    }

    public DepartmentRepresentation setMostRecentPost(LocalDateTime mostRecentPost) {
        this.mostRecentPost = mostRecentPost;
        return this;
    }

    public Long getMemberCount() {
        return memberCount;
    }

    public DepartmentRepresentation setMemberCount(Long memberCount) {
        this.memberCount = memberCount;
        return this;
    }

    public Long getMemberCountAllTime() {
        return memberCountAllTime;
    }

    public DepartmentRepresentation setMemberCountAllTime(Long memberCountAllTime) {
        this.memberCountAllTime = memberCountAllTime;
        return this;
    }

    public LocalDateTime getMostRecentMember() {
        return mostRecentMember;
    }

    public DepartmentRepresentation setMostRecentMember(LocalDateTime mostRecentMember) {
        this.mostRecentMember = mostRecentMember;
        return this;
    }

    public List<ResourceTaskRepresentation> getTasks() {
        return tasks;
    }

    public DepartmentRepresentation setTasks(List<ResourceTaskRepresentation> tasks) {
        this.tasks = tasks;
        return this;
    }

    public List<BoardRepresentation> getBoards() {
        return boards;
    }

    public DepartmentRepresentation setBoards(List<BoardRepresentation> boards) {
        this.boards = boards;
        return this;
    }

    public List<DepartmentDashboardRepresentation.OrganizationRepresentation> getOrganizations() {
        return organizations;
    }

    public DepartmentRepresentation setOrganizations(List<DepartmentDashboardRepresentation.OrganizationRepresentation> organizations) {
        this.organizations = organizations;
        return this;
    }

    public InvoiceCollection getInvoices() {
        return invoices;
    }

    public DepartmentRepresentation setInvoices(InvoiceCollection invoices) {
        this.invoices = invoices;
        return this;
    }

}
