package hr.prism.board.domain;

import hr.prism.board.enums.ExistingRelation;

import javax.persistence.*;
import java.time.LocalDateTime;

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
    
    @Column(name = "existing_relation")
    @Enumerated(value = EnumType.STRING)
    private ExistingRelation existingRelation;
    
    @Column(name = "existing_relation_explanation")
    private String existingRelationExplanation;
    
    @Column(name = "apply_website")
    private String applyWebsite;
    
    @OneToOne
    @JoinColumn(name = "apply_document_id")
    private Document applyDocument;
    
    @Column(name = "apply_email")
    private String applyEmail;
    
    @Column(name = "live_timestamp", nullable = false)
    private LocalDateTime liveTimestamp;
    
    @Column(name = "dead_timestamp", nullable = false)
    private LocalDateTime deadTimestamp;
    
    public String getOrganizationName() {
        return organizationName;
    }
    
    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }
    
    public ExistingRelation getExistingRelation() {
        return existingRelation;
    }
    
    public void setExistingRelation(ExistingRelation existingRelation) {
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
    
    public LocalDateTime getLiveTimestamp() {
        return liveTimestamp;
    }
    
    public void setLiveTimestamp(LocalDateTime liveTimestamp) {
        this.liveTimestamp = liveTimestamp;
    }
    
    public LocalDateTime getDeadTimestamp() {
        return deadTimestamp;
    }
    
    public void setDeadTimestamp(LocalDateTime deadTimestamp) {
        this.deadTimestamp = deadTimestamp;
    }
    
}
