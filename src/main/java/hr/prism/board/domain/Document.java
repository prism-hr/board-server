package hr.prism.board.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "document")
public class Document extends BoardEntity {
    
    @Column(name = "cloudinary_id", nullable = false)
    private String cloudinaryId;
    
    @Column(name = "cloudinary_url", nullable = false)
    private String cloudinaryUrl;
    
    @Column(name = "file_name")
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
}
