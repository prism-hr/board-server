package hr.prism.board.domain;

import hr.prism.board.enums.ExistingRelation;
import hr.prism.board.enums.Scope;
import hr.prism.board.representation.PostResponseReadinessRepresentation;
import hr.prism.board.utils.BoardUtils;
import org.hibernate.validator.constraints.Email;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@DiscriminatorValue(value = Scope.Value.POST)
@NamedEntityGraph(
    name = "post.extended",
    attributeNodes = {
        @NamedAttributeNode(value = "parent", subgraph = "board"),
        @NamedAttributeNode(value = "location"),
        @NamedAttributeNode(value = "applyDocument"),
        @NamedAttributeNode(value = "categories")},
    subgraphs = {
        @NamedSubgraph(
            name = "board",
            attributeNodes = {
                @NamedAttributeNode(value = "parent", subgraph = "department")}),
        @NamedSubgraph(
            name = "department",
            attributeNodes = {
                @NamedAttributeNode(value = "parent", subgraph = "university"),
                @NamedAttributeNode(value = "documentLogo")}),
        @NamedSubgraph(
            name = "university",
            attributeNodes = {
                @NamedAttributeNode(value = "documentLogo")})})
public class Post extends Resource {

    @Column(name = "description")
    private String description;

    @Column(name = "organization_name", nullable = false)
    private String organizationName;

    @Column(name = "organization_logo")
    private String organizationLogo;

    @Column(name = "existing_relation")
    @Enumerated(EnumType.STRING)
    private ExistingRelation existingRelation;

    @Column(name = "existing_relation_explanation")
    private String existingRelationExplanation;

    @Column(name = "apply_website")
    private String applyWebsite;

    @OneToOne
    @JoinColumn(name = "apply_document_id")
    private Document applyDocument;

    @Email
    @Column(name = "apply_email")
    private String applyEmail;

    @Column(name = "apply_email_display")
    private String applyEmailDisplay;

    @Column(name = "live_timestamp", nullable = false)
    private LocalDateTime liveTimestamp;

    @Column(name = "dead_timestamp", nullable = false)
    private LocalDateTime deadTimestamp;

    @Column(name = "view_count")
    private Long viewCount;

    @Column(name = "referral_count")
    private Long referralCount;

    @Column(name = "response_count")
    private Long responseCount;

    @Column(name = "last_view_timestamp")
    private LocalDateTime lastViewTimestamp;

    @Column(name = "last_referral_timestamp")
    private LocalDateTime lastReferralTimestamp;

    @Column(name = "last_response_timestamp")
    private LocalDateTime lastResponseTimestamp;

    @Transient
    private boolean exposeApplyData;

    @Transient
    private PostResponseReadinessRepresentation responseReadiness;

    @Transient
    private ResourceEvent referral;

    @Transient
    private ResourceEvent response;

    @Override
    public void setDocumentLogo(Document documentLogo) {
    }

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

    public String getOrganizationLogo() {
        return organizationLogo;
    }

    public void setOrganizationLogo(String organizationLogo) {
        this.organizationLogo = organizationLogo;
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
        this.applyEmailDisplay = BoardUtils.obfuscateEmail(applyEmail);
    }

    public String getApplyEmailDisplay() {
        return applyEmailDisplay;
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

    public Long getViewCount() {
        return viewCount;
    }

    public void setViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }

    public Long getReferralCount() {
        return referralCount;
    }

    public void setReferralCount(Long referralCount) {
        this.referralCount = referralCount;
    }

    public Long getResponseCount() {
        return responseCount;
    }

    public void setResponseCount(Long responseCount) {
        this.responseCount = responseCount;
    }

    public LocalDateTime getLastViewTimestamp() {
        return lastViewTimestamp;
    }

    public void setLastViewTimestamp(LocalDateTime lastViewTimestamp) {
        this.lastViewTimestamp = lastViewTimestamp;
    }

    public LocalDateTime getLastReferralTimestamp() {
        return lastReferralTimestamp;
    }

    public void setLastReferralTimestamp(LocalDateTime lastReferralTimestamp) {
        this.lastReferralTimestamp = lastReferralTimestamp;
    }

    public LocalDateTime getLastResponseTimestamp() {
        return lastResponseTimestamp;
    }

    public void setLastResponseTimestamp(LocalDateTime lastResponseTimestamp) {
        this.lastResponseTimestamp = lastResponseTimestamp;
    }

    public boolean isExposeApplyData() {
        return exposeApplyData;
    }

    public void setExposeApplyData(boolean exposeApplyData) {
        this.exposeApplyData = exposeApplyData;
    }

    public PostResponseReadinessRepresentation getResponseReadiness() {
        return responseReadiness;
    }

    public void setResponseReadiness(PostResponseReadinessRepresentation responseReadiness) {
        this.responseReadiness = responseReadiness;
    }

    public ResourceEvent getReferral() {
        return referral;
    }

    public void setReferral(ResourceEvent referral) {
        this.referral = referral;
    }

    public ResourceEvent getResponse() {
        return response;
    }

    public void setResponse(ResourceEvent response) {
        this.response = response;
    }

    @Override
    public String getHandle() {
        return getParent().getHandle() + "/" + getId();
    }

}
