package hr.prism.board.representation;

public class DepartmentRepresentation {

    private Long id;

    private String name;

    private DocumentRepresentation documentLogo;

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
