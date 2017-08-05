package hr.prism.board.domain;

import hr.prism.board.enums.SocialNetwork;

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

    @Column(name = "website")
    private String website;

    @Column(name = "covering_note")
    private String coveringNote;

    @Enumerated(EnumType.STRING)
    @Column(name = "social_network")
    private SocialNetwork socialNetwork;

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

    public String getWebsite() {
        return website;
    }

    public ResourceEvent setWebsite(String website) {
        this.website = website;
        return this;
    }

    public String getCoveringNote() {
        return coveringNote;
    }

    public ResourceEvent setCoveringNote(String coveringNote) {
        this.coveringNote = coveringNote;
        return this;
    }

    public SocialNetwork getSocialNetwork() {
        return socialNetwork;
    }

    public ResourceEvent setSocialNetwork(SocialNetwork socialNetwork) {
        this.socialNetwork = socialNetwork;
        return this;
    }

}
