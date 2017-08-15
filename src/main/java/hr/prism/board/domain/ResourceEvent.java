package hr.prism.board.domain;

import javax.persistence.*;

@Entity
@Table(name = "resource_event")
public class ResourceEvent extends BoardEntity {

    @ManyToOne
    @JoinColumn(name = "resource_id", nullable = false)
    private Resource resource;

    @Enumerated(EnumType.STRING)
    @Column(name = "event", nullable = false)
    private hr.prism.board.enums.ResourceEvent event;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "ip_address")
    private String ipAddress;

    @ManyToOne
    @JoinColumn(name = "document_resume_id")
    private Document documentResume;

    @Column(name = "website_resume")
    private String websiteResume;

    @Column(name = "covering_note")
    private String coveringNote;

    public Resource getResource() {
        return resource;
    }

    public ResourceEvent setResource(Resource resource) {
        this.resource = resource;
        return this;
    }

    public hr.prism.board.enums.ResourceEvent getEvent() {
        return event;
    }

    public ResourceEvent setEvent(hr.prism.board.enums.ResourceEvent event) {
        this.event = event;
        return this;
    }

    public User getUser() {
        return user;
    }

    public ResourceEvent setUser(User user) {
        this.user = user;
        return this;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public ResourceEvent setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
        return this;
    }

    public Document getDocumentResume() {
        return documentResume;
    }

    public ResourceEvent setDocumentResume(Document documentResume) {
        this.documentResume = documentResume;
        return this;
    }

    public String getWebsiteResume() {
        return websiteResume;
    }

    public ResourceEvent setWebsiteResume(String websiteResume) {
        this.websiteResume = websiteResume;
        return this;
    }

    public String getCoveringNote() {
        return coveringNote;
    }

    public ResourceEvent setCoveringNote(String coveringNote) {
        this.coveringNote = coveringNote;
        return this;
    }

}
