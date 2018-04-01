package hr.prism.board.representation;

import hr.prism.board.enums.MemberCategory;

import java.util.List;

@SuppressWarnings("unused")
public class DepartmentRepresentation extends ResourceRepresentation<DepartmentRepresentation> {

    private String summary;

    private UniversityRepresentation university;

    private DocumentRepresentation documentLogo;

    private String handle;

    private String customerId;

    private Long postCount;

    private Long postCountAllTime;

    private Long memberCount;

    private Long memberCountAllTime;

    private List<MemberCategory> memberCategories;

    private List<ResourceTaskRepresentation> tasks;

    private List<OrganizationSummaryRepresentation> organizations;

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

    public List<MemberCategory> getMemberCategories() {
        return memberCategories;
    }

    public DepartmentRepresentation setMemberCategories(List<MemberCategory> memberCategories) {
        this.memberCategories = memberCategories;
        return this;
    }

    public List<ResourceTaskRepresentation> getTasks() {
        return tasks;
    }

    public DepartmentRepresentation setTasks(List<ResourceTaskRepresentation> tasks) {
        this.tasks = tasks;
        return this;
    }

    public List<OrganizationSummaryRepresentation> getOrganizations() {
        return organizations;
    }

    public DepartmentRepresentation setOrganizations(List<OrganizationSummaryRepresentation> organizations) {
        this.organizations = organizations;
        return this;
    }

}
