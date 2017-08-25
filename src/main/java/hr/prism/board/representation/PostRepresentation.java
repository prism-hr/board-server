package hr.prism.board.representation;

import hr.prism.board.enums.ExistingRelation;
import hr.prism.board.enums.MemberCategory;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;

public class PostRepresentation extends ResourceRepresentation<PostRepresentation> {

    private String description;

    private String organizationName;

    private LocationRepresentation location;

    private ExistingRelation existingRelation;

    private LinkedHashMap<String, Object> existingRelationExplanation;

    private List<String> postCategories;

    private List<MemberCategory> memberCategories;

    private String applyWebsite;

    private DocumentRepresentation applyDocument;

    private String applyEmail;

    private BoardRepresentation board;

    private LocalDateTime liveTimestamp;

    private LocalDateTime deadTimestamp;

    private Long viewCount;

    private Long referralCount;

    private Long responseCount;

    private LocalDateTime lastViewTimestamp;

    private LocalDateTime lastReferralTimestamp;

    private LocalDateTime lastResponseTimestamp;

    private boolean responded;

    public String getDescription() {
        return description;
    }

    public PostRepresentation setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public PostRepresentation setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
        return this;
    }

    public LocationRepresentation getLocation() {
        return location;
    }

    public PostRepresentation setLocation(LocationRepresentation location) {
        this.location = location;
        return this;
    }

    public ExistingRelation getExistingRelation() {
        return existingRelation;
    }

    public PostRepresentation setExistingRelation(ExistingRelation existingRelation) {
        this.existingRelation = existingRelation;
        return this;
    }

    public LinkedHashMap<String, Object> getExistingRelationExplanation() {
        return existingRelationExplanation;
    }

    public PostRepresentation setExistingRelationExplanation(LinkedHashMap<String, Object> existingRelationExplanation) {
        this.existingRelationExplanation = existingRelationExplanation;
        return this;
    }

    public List<String> getPostCategories() {
        return postCategories;
    }

    public PostRepresentation setPostCategories(List<String> postCategories) {
        this.postCategories = postCategories;
        return this;
    }

    public List<MemberCategory> getMemberCategories() {
        return memberCategories;
    }

    public PostRepresentation setMemberCategories(List<MemberCategory> memberCategories) {
        this.memberCategories = memberCategories;
        return this;
    }

    public String getApplyWebsite() {
        return applyWebsite;
    }

    public PostRepresentation setApplyWebsite(String applyWebsite) {
        this.applyWebsite = applyWebsite;
        return this;
    }

    public DocumentRepresentation getApplyDocument() {
        return applyDocument;
    }

    public PostRepresentation setApplyDocument(DocumentRepresentation applyDocument) {
        this.applyDocument = applyDocument;
        return this;
    }

    public String getApplyEmail() {
        return applyEmail;
    }

    public PostRepresentation setApplyEmail(String applyEmail) {
        this.applyEmail = applyEmail;
        return this;
    }

    public BoardRepresentation getBoard() {
        return board;
    }

    public PostRepresentation setBoard(BoardRepresentation board) {
        this.board = board;
        return this;
    }

    public LocalDateTime getLiveTimestamp() {
        return liveTimestamp;
    }

    public PostRepresentation setLiveTimestamp(LocalDateTime liveTimestamp) {
        this.liveTimestamp = liveTimestamp;
        return this;
    }

    public LocalDateTime getDeadTimestamp() {
        return deadTimestamp;
    }

    public PostRepresentation setDeadTimestamp(LocalDateTime deadTimestamp) {
        this.deadTimestamp = deadTimestamp;
        return this;
    }

    public Long getViewCount() {
        return viewCount;
    }

    public PostRepresentation setViewCount(Long viewCount) {
        this.viewCount = viewCount;
        return this;
    }

    public Long getReferralCount() {
        return referralCount;
    }

    public PostRepresentation setReferralCount(Long referralCount) {
        this.referralCount = referralCount;
        return this;
    }

    public Long getResponseCount() {
        return responseCount;
    }

    public PostRepresentation setResponseCount(Long responseCount) {
        this.responseCount = responseCount;
        return this;
    }

    public LocalDateTime getLastViewTimestamp() {
        return lastViewTimestamp;
    }

    public PostRepresentation setLastViewTimestamp(LocalDateTime lastViewTimestamp) {
        this.lastViewTimestamp = lastViewTimestamp;
        return this;
    }

    public LocalDateTime getLastReferralTimestamp() {
        return lastReferralTimestamp;
    }

    public PostRepresentation setLastReferralTimestamp(LocalDateTime lastReferralTimestamp) {
        this.lastReferralTimestamp = lastReferralTimestamp;
        return this;
    }

    public LocalDateTime getLastResponseTimestamp() {
        return lastResponseTimestamp;
    }

    public PostRepresentation setLastResponseTimestamp(LocalDateTime lastResponseTimestamp) {
        this.lastResponseTimestamp = lastResponseTimestamp;
        return this;
    }

    public boolean isResponded() {
        return responded;
    }

    public PostRepresentation setResponded(boolean responded) {
        this.responded = responded;
        return this;
    }

}
