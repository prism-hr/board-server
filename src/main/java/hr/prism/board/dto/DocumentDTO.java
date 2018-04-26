package hr.prism.board.dto;

import hr.prism.board.definition.DocumentDefinition;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.validation.constraints.NotNull;

public class DocumentDTO implements DocumentDefinition {

    private Long id;

    @NotNull
    private String cloudinaryId;

    @NotNull
    private String cloudinaryUrl;

    @NotNull
    private String fileName;

    public Long getId() {
        return id;
    }

    public DocumentDTO setId(Long id) {
        this.id = id;
        return this;
    }

    public String getCloudinaryId() {
        return cloudinaryId;
    }

    public DocumentDTO setCloudinaryId(String cloudinaryId) {
        this.cloudinaryId = cloudinaryId;
        return this;
    }

    public String getCloudinaryUrl() {
        return cloudinaryUrl;
    }

    public DocumentDTO setCloudinaryUrl(String cloudinaryUrl) {
        this.cloudinaryUrl = cloudinaryUrl;
        return this;
    }

    public String getFileName() {
        return fileName;
    }

    public DocumentDTO setFileName(String fileName) {
        this.fileName = fileName;
        return this;
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

        DocumentDTO that = (DocumentDTO) other;
        return new EqualsBuilder()
            .append(cloudinaryId, that.cloudinaryId)
            .isEquals();
    }

}
