package hr.prism.board.dto;

import hr.prism.board.enums.ExistingRelation;
import hr.prism.board.enums.MemberCategory;
import org.hibernate.validator.constraints.Email;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PostPatchDTO extends ResourcePatchDTO<PostPatchDTO> {

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<String> summary;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<String> description;

    @Valid
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<OrganizationDTO> organization;

    @Valid
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<LocationDTO> location;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<ExistingRelation> existingRelation;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<Map<String, Object>> existingRelationExplanation;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<List<String>> postCategories;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<List<MemberCategory>> memberCategories;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<String> applyWebsite;

    @Valid
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<DocumentDTO> applyDocument;

    @Email
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<String> applyEmail;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<LocalDateTime> liveTimestamp;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<LocalDateTime> deadTimestamp;

    public Optional<String> getSummary() {
        return summary;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public PostPatchDTO setSummary(Optional<String> summary) {
        this.summary = summary;
        return this;
    }

    public Optional<String> getDescription() {
        return description;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public PostPatchDTO setDescription(Optional<String> description) {
        this.description = description;
        return this;
    }

    public Optional<OrganizationDTO> getOrganization() {
        return organization;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public PostPatchDTO setOrganization(Optional<OrganizationDTO> organization) {
        this.organization = organization;
        return this;
    }

    public Optional<LocationDTO> getLocation() {
        return location;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public PostPatchDTO setLocation(Optional<LocationDTO> location) {
        this.location = location;
        return this;
    }

    public Optional<ExistingRelation> getExistingRelation() {
        return existingRelation;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public PostPatchDTO setExistingRelation(Optional<ExistingRelation> existingRelation) {
        this.existingRelation = existingRelation;
        return this;
    }

    public Optional<Map<String, Object>> getExistingRelationExplanation() {
        return existingRelationExplanation;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public PostPatchDTO setExistingRelationExplanation(Optional<Map<String, Object>> existingRelationExplanation) {
        this.existingRelationExplanation = existingRelationExplanation;
        return this;
    }

    public Optional<List<String>> getPostCategories() {
        return postCategories;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public PostPatchDTO setPostCategories(Optional<List<String>> postCategories) {
        this.postCategories = postCategories;
        return this;
    }

    public Optional<List<MemberCategory>> getMemberCategories() {
        return memberCategories;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public PostPatchDTO setMemberCategories(Optional<List<MemberCategory>> memberCategories) {
        this.memberCategories = memberCategories;
        return this;
    }

    public Optional<String> getApplyWebsite() {
        return applyWebsite;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public PostPatchDTO setApplyWebsite(Optional<String> applyWebsite) {
        this.applyWebsite = applyWebsite;
        return this;
    }

    public Optional<DocumentDTO> getApplyDocument() {
        return applyDocument;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public PostPatchDTO setApplyDocument(Optional<DocumentDTO> applyDocument) {
        this.applyDocument = applyDocument;
        return this;
    }

    public Optional<String> getApplyEmail() {
        return applyEmail;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public PostPatchDTO setApplyEmail(Optional<String> applyEmail) {
        this.applyEmail = applyEmail;
        return this;
    }

    public Optional<LocalDateTime> getLiveTimestamp() {
        return liveTimestamp;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public PostPatchDTO setLiveTimestamp(Optional<LocalDateTime> liveTimestamp) {
        this.liveTimestamp = liveTimestamp;
        return this;
    }

    public Optional<LocalDateTime> getDeadTimestamp() {
        return deadTimestamp;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public PostPatchDTO setDeadTimestamp(Optional<LocalDateTime> deadTimestamp) {
        this.deadTimestamp = deadTimestamp;
        return this;
    }

}
