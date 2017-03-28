package hr.prism.board.representation;

import java.util.List;

public class DepartmentRepresentation extends AbstractResourceRepresentation {

    private DocumentRepresentation documentLogo;

    private String handle;

    private List<BoardRepresentation> boards;

    private List<String> memberCategories;

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

    public List<BoardRepresentation> getBoards() {
        return boards;
    }

    public DepartmentRepresentation setBoards(List<BoardRepresentation> boards) {
        this.boards = boards;
        return this;
    }

    public List<String> getMemberCategories() {
        return memberCategories;
    }

    public DepartmentRepresentation setMemberCategories(List<String> memberCategories) {
        this.memberCategories = memberCategories;
        return this;
    }

}
