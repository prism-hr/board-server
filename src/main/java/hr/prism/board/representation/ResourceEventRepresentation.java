package hr.prism.board.representation;

import hr.prism.board.enums.ResourceEvent;
import hr.prism.board.enums.ResourceEventMatch;

import java.time.LocalDateTime;
import java.util.List;

public class ResourceEventRepresentation {

    private Long id;

    private ResourceEvent event;

    private UserRepresentation user;

    private String ipAddress;

    private String referral;

    private DocumentRepresentation documentResume;

    private String websiteResume;

    private String coveringNote;

    private LocalDateTime createdTimestamp;

    private ResourceEventMatch match;

    private boolean viewed;

    private List<ResourceEventRepresentation> history;

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

    public String getReferral() {
        return referral;
    }

    public ResourceEventRepresentation setReferral(String referral) {
        this.referral = referral;
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

    public ResourceEventMatch getMatch() {
        return match;
    }

    public ResourceEventRepresentation setMatch(ResourceEventMatch match) {
        this.match = match;
        return this;
    }

    public boolean isViewed() {
        return viewed;
    }

    public ResourceEventRepresentation setViewed(boolean viewed) {
        this.viewed = viewed;
        return this;
    }

    public List<ResourceEventRepresentation> getHistory() {
        return history;
    }

    public ResourceEventRepresentation setHistory(List<ResourceEventRepresentation> history) {
        this.history = history;
        return this;
    }

}
