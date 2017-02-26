package hr.prism.board.representation;

public class DocumentRepresentation {

    private String cloudinaryId;

    private String cloudinaryUrl;

    private String fileName;

    public String getCloudinaryId() {
        return cloudinaryId;
    }

    public void setCloudinaryId(String cloudinaryId) {
        this.cloudinaryId = cloudinaryId;
    }

    public String getCloudinaryUrl() {
        return cloudinaryUrl;
    }

    public void setCloudinaryUrl(String cloudinaryUrl) {
        this.cloudinaryUrl = cloudinaryUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public DocumentRepresentation withCloudinaryId(final String cloudinaryId) {
        this.cloudinaryId = cloudinaryId;
        return this;
    }

    public DocumentRepresentation withCloudinaryUrl(final String cloudinaryUrl) {
        this.cloudinaryUrl = cloudinaryUrl;
        return this;
    }

    public DocumentRepresentation withFileName(final String fileName) {
        this.fileName = fileName;
        return this;
    }
}
