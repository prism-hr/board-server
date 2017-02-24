package hr.prism.board.dto;

import javax.validation.constraints.NotNull;

public class DocumentDTO {

    @NotNull
    private String cloudinaryId;

    @NotNull
    private String cloudinaryUrl;

    @NotNull
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

    public DocumentDTO withCloudinaryId(final String cloudinaryId) {
        this.cloudinaryId = cloudinaryId;
        return this;
    }

    public DocumentDTO withCloudinaryUrl(final String cloudinaryUrl) {
        this.cloudinaryUrl = cloudinaryUrl;
        return this;
    }

    public DocumentDTO withFileName(final String fileName) {
        this.fileName = fileName;
        return this;
    }
}
