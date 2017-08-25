package hr.prism.board.dto;

import org.hibernate.validator.constraints.URL;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class ResourceEventDTO {

    @Valid
    @NotNull
    private DocumentDTO documentResume;

    @URL
    private String websiteResume;

    @NotNull
    private Boolean defaultResume;

    @NotNull
    @Size(max = 1000)
    private String coveringNote;

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

    public Boolean getDefaultResume() {
        return defaultResume;
    }

    public ResourceEventDTO setDefaultResume(Boolean defaultResume) {
        this.defaultResume = defaultResume;
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
