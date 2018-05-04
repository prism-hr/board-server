package hr.prism.board.domain;

import hr.prism.board.enums.ExistingRelation;
import hr.prism.board.enums.Scope;
import hr.prism.board.value.DemographicDataStatus;
import hr.prism.board.value.PostStatistics;
import org.hibernate.validator.constraints.Email;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

import static hr.prism.board.enums.Scope.Value.POST;
import static hr.prism.board.utils.BoardUtils.obfuscateEmail;
import static java.util.Optional.ofNullable;

@Entity
@DiscriminatorValue(value = POST)
@NamedEntityGraph(
    name = "post.extended",
    attributeNodes = {
        @NamedAttributeNode(value = "parent", subgraph = "board"),
        @NamedAttributeNode(value = "organization"),
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
@NamedNativeQuery(
    name = "postStatistics",
    query =
        "SELECT COALESCE(SUM(IF(post.state = 'ACCEPTED', 1, 0)), 0) AS countLive, " +
            "COALESCE(SUM(IF(post.created_timestamp >= MAKEDATE(YEAR(CURRENT_DATE()) - IF(" +
            "MONTH(CURRENT_DATE()) > 9, 0, 1), 10), 1, 0)), 0) AS countThisYear, " +
            "COUNT(post.id) AS countAllTime, " +
            "MAX(post.created_timestamp) AS mostRecent, " +
            "COALESCE(SUM(IF(post.state = 'ACCEPTED', post.view_count, 0)), 0) AS viewCountLive, " +
            "COALESCE(SUM(IF(post.created_timestamp >= MAKEDATE(YEAR(CURRENT_DATE()) - IF(" +
            "MONTH(CURRENT_DATE()) > 9, 0, 1), 10), post.view_count, 0)), 0) AS viewCountThisYear, " +
            "COALESCE(SUM(post.view_count), 0)  AS viewCountAllTime, " +
            "MAX(post.last_view_timestamp) AS mostRecentView, " +
            "COALESCE(SUM(IF(post.state = 'ACCEPTED', post.referral_count, 0)), 0) AS referralCountLive, " +
            "COALESCE(SUM(IF(post.created_timestamp >= MAKEDATE(YEAR(CURRENT_DATE()) - IF(" +
            "MONTH(CURRENT_DATE()) > 9, 0, 1), 10), post.referral_count, 0)), 0) AS referralCountThisYear, " +
            "COALESCE(SUM(post.referral_count), 0) AS referralCountAllTime, " +
            "MAX(post.last_referral_timestamp) AS mostRecentReferral, " +
            "COALESCE(SUM(IF(post.state = 'ACCEPTED', post.response_count, 0)), 0) AS responseCountLive, " +
            "COALESCE(SUM(IF(post.created_timestamp >= MAKEDATE(YEAR(CURRENT_DATE()) - IF(" +
            "MONTH(CURRENT_DATE()) > 9, 0, 1), 10), post.response_count, 0)), 0) AS responseCountThisYear, " +
            "COALESCE(SUM(post.response_count), 0) AS responseCountAllTime, " +
            "MAX(post.last_response_timestamp) AS mostRecentResponse " +
            "FROM resource AS post " +
            "INNER JOIN resource AS board " +
            "ON post.parent_id = board.id " +
            "WHERE board.parent_id = :departmentId",
    resultSetMapping = "postStatistics")
@SqlResultSetMapping(
    name = "postStatistics",
    classes = @ConstructorResult(
        targetClass = PostStatistics.class,
        columns = {
            @ColumnResult(name = "countLive", type = Long.class),
            @ColumnResult(name = "countThisYear", type = Long.class),
            @ColumnResult(name = "countAllTime", type = Long.class),
            @ColumnResult(name = "mostRecent", type = LocalDateTime.class),
            @ColumnResult(name = "viewCountLive", type = Long.class),
            @ColumnResult(name = "viewCountThisYear", type = Long.class),
            @ColumnResult(name = "viewCountAllTime", type = Long.class),
            @ColumnResult(name = "mostRecentView", type = LocalDateTime.class),
            @ColumnResult(name = "referralCountLive", type = Long.class),
            @ColumnResult(name = "referralCountThisYear", type = Long.class),
            @ColumnResult(name = "referralCountAllTime", type = Long.class),
            @ColumnResult(name = "mostRecentReferral", type = LocalDateTime.class),
            @ColumnResult(name = "responseCountLive", type = Long.class),
            @ColumnResult(name = "responseCountThisYear", type = Long.class),
            @ColumnResult(name = "responseCountAllTime", type = Long.class),
            @ColumnResult(name = "mostRecentResponse", type = LocalDateTime.class)}))
@SuppressWarnings("SqlResolve")
public class Post extends Resource {

    @Column(name = "description")
    private String description;

    @ManyToOne
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

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
    private DemographicDataStatus demographicDataStatus;

    @Transient
    private ResourceEvent referral;

    @Transient
    private ResourceEvent response;

    public Post() {
        setScope(Scope.POST);
    }

    @Override
    public void setDocumentLogo(Document documentLogo) {
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
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
        this.applyEmailDisplay = obfuscateEmail(applyEmail);
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

    public DemographicDataStatus getDemographicDataStatus() {
        return demographicDataStatus;
    }

    public void setDemographicDataStatus(DemographicDataStatus demographicDataStatus) {
        this.demographicDataStatus = demographicDataStatus;
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

    @Override
    public List<String> getIndexDataParts() {
        List<String> parts = super.getIndexDataParts();
        parts.add(description);

        ofNullable(location).ifPresent(location -> parts.add(location.getName()));
        ofNullable(organization).ifPresent(organization -> parts.add(organization.getName()));
        return parts;
    }

}
