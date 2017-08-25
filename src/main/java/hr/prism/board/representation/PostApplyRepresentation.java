package hr.prism.board.representation;

public class PostApplyRepresentation {

    private String applyWebsite;

    private DocumentRepresentation applyDocument;

    private String applyEmail;

    public String getApplyWebsite() {
        return applyWebsite;
    }

    public PostApplyRepresentation setApplyWebsite(String applyWebsite) {
        this.applyWebsite = applyWebsite;
        return this;
    }

    public DocumentRepresentation getApplyDocument() {
        return applyDocument;
    }

    public PostApplyRepresentation setApplyDocument(DocumentRepresentation applyDocument) {
        this.applyDocument = applyDocument;
        return this;
    }

    public String getApplyEmail() {
        return applyEmail;
    }

    public PostApplyRepresentation setApplyEmail(String applyEmail) {
        this.applyEmail = applyEmail;
        return this;
    }

}
