package hr.prism.board.representation;

public class UniversityRepresentation extends ResourceRepresentation<UniversityRepresentation> {

    private String homepage;

    private DocumentRepresentation documentLogo;

    private String handle;

    public String getHomepage() {
        return homepage;
    }

    public UniversityRepresentation setHomepage(String homepage) {
        this.homepage = homepage;
        return this;
    }

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
