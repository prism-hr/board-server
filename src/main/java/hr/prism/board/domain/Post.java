package hr.prism.board.domain;

import hr.prism.board.enums.ExistingRelation;
import hr.prism.board.enums.Scope;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.URL;

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
    subgraphs = {
        @NamedSubgraph(
            name = "board",
            attributeNodes = {
                @NamedAttributeNode(value = "parent", subgraph = "department"),
                @NamedAttributeNode(value = "categories"),
                @NamedAttributeNode(value = "documentLogo")}),
        @NamedSubgraph(
            name = "department",
            attributeNodes = {
                @NamedAttributeNode(value = "categories"),
                @NamedAttributeNode(value = "documentLogo")})})
public class Post extends Resource {

    @Column(name = "description")
    private String description;

    @Column(name = "organization_name", nullable = false)
    private String organizationName;

    @Column(name = "existing_relation")
    @Enumerated(value = EnumType.STRING)
    private ExistingRelation existingRelation;

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

    @Column(name = "forward_candidates")
    private Boolean forwardCandidates;

    @Column(name = "live_timestamp", nullable = false)
    private LocalDateTime liveTimestamp;

    @Column(name = "dead_timestamp", nullable = false)
    private LocalDateTime deadTimestamp;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

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

    public Boolean getForwardCandidates() {
        return forwardCandidates;
    }

    public void setForwardCandidates(Boolean forwardCandidates) {
        this.forwardCandidates = forwardCandidates;
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

    @Override
    public String getHandle() {
        return getParent().getHandle() + "/" + getId();
    }

}
