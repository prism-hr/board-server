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
}
