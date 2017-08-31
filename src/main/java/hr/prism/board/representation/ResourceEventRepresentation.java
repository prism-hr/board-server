package hr.prism.board.representation;

import hr.prism.board.enums.ResourceEvent;

import java.time.LocalDateTime;

public class ResourceEventRepresentation {

    private Long id;

    private ResourceEvent event;

    private UserRepresentation user;

    private String ipAddress;

    private DocumentRepresentation documentResume;

    private String websiteResume;

    private String coveringNote;

    private LocalDateTime createdTimestamp;

    private boolean viewed;

    public Long getId() {
        return id;
    }

    public ResourceEventRepresentation setId(Long id) {
        this.id = id;
        return this;
    }

    public ResourceEvent getEvent() {
        return event;
    }

    public ResourceEventRepresentation setEvent(ResourceEvent event) {
        this.event = event;
        return this;
    }

    public UserRepresentation getUser() {
        return user;
    }

    public ResourceEventRepresentation setUser(UserRepresentation user) {
        this.user = user;
        return this;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public ResourceEventRepresentation setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
        return this;
    }

    public boolean isViewed() {
        return viewed;
    }

    public ResourceEventRepresentation setViewed(boolean viewed) {
        this.viewed = viewed;
        return this;
    }

    public DocumentRepresentation getDocumentResume() {
        return documentResume;
    }

    public ResourceEventRepresentation setDocumentResume(DocumentRepresentation documentResume) {
        this.documentResume = documentResume;
        return this;
    }

    public String getWebsiteResume() {
        return websiteResume;
    }

    public ResourceEventRepresentation setWebsiteResume(String websiteResume) {
        this.websiteResume = websiteResume;
        return this;
    }

    public String getCoveringNote() {
        return coveringNote;
    }

    public ResourceEventRepresentation setCoveringNote(String coveringNote) {
        this.coveringNote = coveringNote;
        return this;
    }

    public LocalDateTime getCreatedTimestamp() {
        return createdTimestamp;
    }

    public ResourceEventRepresentation setCreatedTimestamp(LocalDateTime createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
        return this;
    }

}
