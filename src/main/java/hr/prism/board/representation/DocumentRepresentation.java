package hr.prism.board.representation;

public class DocumentRepresentation {
    
    private String cloudinaryId;
    
    private String cloudinaryUrl;
    
    private String fileName;
    
    public String getCloudinaryId() {
        return cloudinaryId;
    }
    
    public DocumentRepresentation setCloudinaryId(String cloudinaryId) {
        this.cloudinaryId = cloudinaryId;
        return this;
    }
    
    public String getCloudinaryUrl() {
        return cloudinaryUrl;
    }
    
    public DocumentRepresentation setCloudinaryUrl(String cloudinaryUrl) {
        this.cloudinaryUrl = cloudinaryUrl;
        return this;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public DocumentRepresentation setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }
}
