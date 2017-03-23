package hr.prism.board.domain;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.URL;

import javax.persistence.*;

@Entity
@DiscriminatorValue(value = Scope.Value.POST)
public class Post extends Resource {

    @Column(name = "organization_name", nullable = false)
    private String organizationName;

    @ManyToOne
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @Column(name = "existing_relation", nullable = false)
    private Boolean existingRelation;

    @URL
    @Column(name = "apply_website")
    private String applyWebsite;

    @OneToOne
    @JoinColumn(name = "apply_document_id")
    private Document applyDocument;

    @Email
    @Column(name = "apply_email")
    private String applyEmail;

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Boolean getExistingRelation() {
        return existingRelation;
    }

    public void setExistingRelation(Boolean existingRelation) {
        this.existingRelation = existingRelation;
    }

    public String getApplyWebsite() {
        return applyWebsite;
    }

    public void setApplyWebsite(String applyWebsite) {
        this.applyWebsite = applyWebsite;
    }

    public Document getApplyDocument() {
        return applyDocument;
    }

    public void setApplyDocument(Document applyDocument) {
        this.applyDocument = applyDocument;
    }

    public String getApplyEmail() {
        return applyEmail;
    }

    public void setApplyEmail(String applyEmail) {
        this.applyEmail = applyEmail;
    }
    
}
