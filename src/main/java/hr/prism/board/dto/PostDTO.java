package hr.prism.board.dto;

import hr.prism.board.enums.ExistingRelation;
import hr.prism.board.enums.MemberCategory;
import org.hibernate.validator.constraints.Email;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class PostDTO extends ResourceDTO<PostDTO> {

    @Size(min = 3, max = 1000)
    private String summary;

    private String description;

    @Valid
    @NotNull
    private OrganizationDTO organization;

    @Valid
    @NotNull
    private LocationDTO location;

    private ExistingRelation existingRelation;

    private Map<String, Object> existingRelationExplanation;

    private List<String> postCategories;

    private List<MemberCategory> memberCategories;

    private String applyWebsite;

    @Valid
    private DocumentDTO applyDocument;

    @Email
    private String applyEmail;

    private LocalDateTime liveTimestamp;

    private LocalDateTime deadTimestamp;

    public String getSummary() {
        return summary;
    }

    public PostDTO setSummary(String summary) {
        this.summary = summary;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public PostDTO setDescription(String description) {
        this.description = description;
        return this;
    }

    public OrganizationDTO getOrganization() {
        return organization;
    }

    public PostDTO setOrganization(OrganizationDTO organization) {
        this.organization = organization;
        return this;
    }

    public LocationDTO getLocation() {
        return location;
    }

    public PostDTO setLocation(LocationDTO location) {
        this.location = location;
        return this;
    }

    public ExistingRelation getExistingRelation() {
        return existingRelation;
    }

    public PostDTO setExistingRelation(ExistingRelation existingRelation) {
        this.existingRelation = existingRelation;
        return this;
    }

    public Map<String, Object> getExistingRelationExplanation() {
        return existingRelationExplanation;
    }

    public PostDTO setExistingRelationExplanation(Map<String, Object> existingRelationExplanation) {
        this.existingRelationExplanation = existingRelationExplanation;
        return this;
    }

    public List<String> getPostCategories() {
        return postCategories;
    }

    public PostDTO setPostCategories(List<String> postCategories) {
        this.postCategories = postCategories;
        return this;
    }

    public List<MemberCategory> getMemberCategories() {
        return memberCategories;
    }

    public PostDTO setMemberCategories(List<MemberCategory> memberCategories) {
        this.memberCategories = memberCategories;
        return this;
    }

    public String getApplyWebsite() {
        return applyWebsite;
    }

    public PostDTO setApplyWebsite(String applyWebsite) {
        this.applyWebsite = applyWebsite;
        return this;
    }

    public DocumentDTO getApplyDocument() {
        return applyDocument;
    }

    public PostDTO setApplyDocument(DocumentDTO applyDocument) {
        this.applyDocument = applyDocument;
        return this;
    }

    public String getApplyEmail() {
        return applyEmail;
    }

    public PostDTO setApplyEmail(String applyEmail) {
        this.applyEmail = applyEmail;
        return this;
    }

    public LocalDateTime getLiveTimestamp() {
        return liveTimestamp;
    }

    public PostDTO setLiveTimestamp(LocalDateTime liveTimestamp) {
        this.liveTimestamp = liveTimestamp;
        return this;
    }

    public LocalDateTime getDeadTimestamp() {
        return deadTimestamp;
    }

    public PostDTO setDeadTimestamp(LocalDateTime deadTimestamp) {
        this.deadTimestamp = deadTimestamp;
        return this;
    }

}
