package hr.prism.board.representation;

import hr.prism.board.enums.MemberCategory;

import java.util.List;

public class DepartmentRepresentation extends ResourceRepresentation<DepartmentRepresentation> {

    private DocumentRepresentation documentLogo;

    private String handle;

    private List<MemberCategory> memberCategories;

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

}
