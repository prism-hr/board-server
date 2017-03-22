package hr.prism.board.dto;

import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.NotNull;

public class DocumentDTO {

    @NotNull
    private String cloudinaryId;

    @URL
    @NotNull
    private String cloudinaryUrl;

    @NotNull
    private String fileName;

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
}
