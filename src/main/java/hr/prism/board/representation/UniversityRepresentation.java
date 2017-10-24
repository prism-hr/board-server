package hr.prism.board.representation;

public class UniversityRepresentation extends ResourceRepresentation<UniversityRepresentation> {

    private DocumentRepresentation documentLogo;

    private String handle;

    public DocumentRepresentation getDocumentLogo() {
        return documentLogo;
    }

    public UniversityRepresentation setDocumentLogo(DocumentRepresentation documentLogo) {
        this.documentLogo = documentLogo;
        return this;
    }

    public String getHandle() {
        return handle;
    }

    public UniversityRepresentation setHandle(String handle) {
        this.handle = handle;
        return this;
    }

}
