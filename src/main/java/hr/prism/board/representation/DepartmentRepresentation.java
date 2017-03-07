package hr.prism.board.representation;

import java.util.List;

public class DepartmentRepresentation {

    private Long id;

    private String name;

    private DocumentRepresentation documentLogo;

    private List<BoardRepresentation> boards;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DocumentRepresentation getDocumentLogo() {
        return documentLogo;
    }

    public void setDocumentLogo(DocumentRepresentation documentLogo) {
        this.documentLogo = documentLogo;
    }

    public List<BoardRepresentation> getBoards() {
        return boards;
    }

    public void setBoards(List<BoardRepresentation> boards) {
        this.boards = boards;
    }

    public DepartmentRepresentation withId(final Long id) {
        this.id = id;
        return this;
    }

    public DepartmentRepresentation withName(final String name) {
        this.name = name;
        return this;
    }

    public DepartmentRepresentation withDocumentLogo(final DocumentRepresentation documentLogo) {
        this.documentLogo = documentLogo;
        return this;
    }

}
