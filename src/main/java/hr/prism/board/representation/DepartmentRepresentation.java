package hr.prism.board.representation;

import hr.prism.board.enums.MemberCategory;

import java.util.List;

@SuppressWarnings("unused")
public class DepartmentRepresentation extends ResourceRepresentation<DepartmentRepresentation> {

    private String summary;

    private UniversityRepresentation university;

    private DocumentRepresentation documentLogo;

    private String handle;

    private List<MemberCategory> memberCategories;

    private String customerId;

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

}
