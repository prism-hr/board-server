package hr.prism.board.domain;

import hr.prism.board.enums.RelationWithDepartment;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.URL;

import javax.persistence.*;

@Entity
@DiscriminatorValue(value = Scope.Value.POST)
@NamedEntityGraph(
    name = "post.extended",
    attributeNodes = {
        @NamedAttributeNode(value = "parent", subgraph = "board"),
        @NamedAttributeNode(value = "location"),
        @NamedAttributeNode(value = "categories"),
        @NamedAttributeNode(value = "applyDocument")},
    subgraphs = @NamedSubgraph(
        name = "board",
        attributeNodes = @NamedAttributeNode("parent")))
public class Post extends Resource {

    @Column(name = "organization_name", nullable = false)
    private String organizationName;

    @ManyToOne
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @Column(name = "existing_relation")
    @Enumerated(value = EnumType.STRING)
    private RelationWithDepartment existingRelation;
    
    @Column(name = "existing_relation_explanation")
    private String existingRelationExplanation;
    
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

    public RelationWithDepartment getExistingRelation() {
        return existingRelation;
    }
    
    public void setExistingRelation(String existingRelation) {
        this.existingRelation = existingRelation;
    }
    
    public String getExistingRelationExplanation() {
        return existingRelationExplanation;
    }
    
    public void setExistingRelationExplanation(String existingRelationExplanation) {
        this.existingRelationExplanation = existingRelationExplanation;
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
