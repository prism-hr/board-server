package hr.prism.board.representation;

import hr.prism.board.enums.MemberCategory;

import java.util.List;

public class DepartmentRepresentation extends ResourceRepresentation<DepartmentRepresentation> {

    private UniversityRepresentation university;

    private DocumentRepresentation documentLogo;

    private String handle;

    private List<MemberCategory> memberCategories;

    private Long boardCount;

    private Long memberCount;

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

    public Long getBoardCount() {
        return boardCount;
    }

    public DepartmentRepresentation setBoardCount(Long boardCount) {
        this.boardCount = boardCount;
        return this;
    }

    public Long getMemberCount() {
        return memberCount;
    }

    public DepartmentRepresentation setMemberCount(Long memberCount) {
        this.memberCount = memberCount;
        return this;
    }

}
