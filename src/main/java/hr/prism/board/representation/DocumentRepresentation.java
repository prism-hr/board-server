package hr.prism.board.representation;

import hr.prism.board.definition.DocumentDefinition;

import java.util.Objects;

public class DocumentRepresentation implements DocumentDefinition {
    
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
    
    @Override
    public int hashCode() {
        return Objects.hash(cloudinaryId);
    }
    
    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        
        return Objects.equals(cloudinaryId, ((DocumentRepresentation) object).getCloudinaryId());
    }
    
}
