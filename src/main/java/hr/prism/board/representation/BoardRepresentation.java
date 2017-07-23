package hr.prism.board.representation;

import hr.prism.board.enums.PostVisibility;

import java.util.List;

public class BoardRepresentation extends ResourceRepresentation<BoardRepresentation> {

    private DocumentRepresentation documentLogo;

    private String handle;

    private DepartmentRepresentation department;

    private List<String> postCategories;

    private PostVisibility defaultPostVisibility;

    public DocumentRepresentation getDocumentLogo() {
        return documentLogo;
    }

    public BoardRepresentation setDocumentLogo(DocumentRepresentation documentLogo) {
        this.documentLogo = documentLogo;
        return this;
    }

    public String getHandle() {
        return handle;
    }

    public BoardRepresentation setHandle(String handle) {
        this.handle = handle;
        return this;
    }

    public DepartmentRepresentation getDepartment() {
        return department;
    }

    public BoardRepresentation setDepartment(DepartmentRepresentation department) {
        this.department = department;
        return this;
    }

    public List<String> getPostCategories() {
        return postCategories;
    }

    public BoardRepresentation setPostCategories(List<String> postCategories) {
        this.postCategories = postCategories;
        return this;
    }

    public PostVisibility getDefaultPostVisibility() {
        return defaultPostVisibility;
    }

    public BoardRepresentation setDefaultPostVisibility(PostVisibility defaultPostVisibility) {
        this.defaultPostVisibility = defaultPostVisibility;
        return this;
    }

}
