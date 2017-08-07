package hr.prism.board.dto;

import org.hibernate.validator.constraints.NotEmpty;

public class ResourceEventDTO {

    private Boolean share;

    private Boolean shareAsDefault;

    private DocumentDTO documentResume;

    private String websiteResume;

    @NotEmpty
    private String coveringNote;

    public Boolean getShare() {
        return share;
    }

    public ResourceEventDTO setShare(Boolean share) {
        this.share = share;
        return this;
    }

    public Boolean getShareAsDefault() {
        return shareAsDefault;
    }

    public ResourceEventDTO setShareAsDefault(Boolean shareAsDefault) {
        this.shareAsDefault = shareAsDefault;
        return this;
    }

    public DocumentDTO getDocumentResume() {
        return documentResume;
    }

    public ResourceEventDTO setDocumentResume(DocumentDTO documentResume) {
        this.documentResume = documentResume;
        return this;
    }

    public String getWebsiteResume() {
        return websiteResume;
    }

    public ResourceEventDTO setWebsiteResume(String websiteResume) {
        this.websiteResume = websiteResume;
        return this;
    }

    public String getCoveringNote() {
        return coveringNote;
    }

    public ResourceEventDTO setCoveringNote(String coveringNote) {
        this.coveringNote = coveringNote;
        return this;
    }

}
