package hr.prism.board.representation;

import hr.prism.board.enums.BoardType;

import java.util.List;

public class BoardRepresentation extends ResourceRepresentation<BoardRepresentation> {

    private BoardType type;

    private DocumentRepresentation documentLogo;

    private String handle;

    private DepartmentRepresentation department;

    private List<String> postCategories;

    private Long postCount;

    private Long authorCount;

    public BoardType getType() {
        return type;
    }

    public BoardRepresentation setType(BoardType type) {
        this.type = type;
        return this;
    }

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

    public Long getPostCount() {
        return postCount;
    }

    public BoardRepresentation setPostCount(Long postCount) {
        this.postCount = postCount;
        return this;
    }

    public Long getAuthorCount() {
        return authorCount;
    }

    public BoardRepresentation setAuthorCount(Long authorCount) {
        this.authorCount = authorCount;
        return this;
    }

}
