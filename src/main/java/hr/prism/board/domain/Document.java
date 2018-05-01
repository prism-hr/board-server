package hr.prism.board.domain;

import hr.prism.board.definition.DocumentDefinition;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "document")
public class Document extends BoardEntity implements DocumentDefinition {

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

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(cloudinaryId)
            .toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        Document document = (Document) other;
        return new EqualsBuilder()
            .append(cloudinaryId, document.cloudinaryId)
            .isEquals();
    }

}
