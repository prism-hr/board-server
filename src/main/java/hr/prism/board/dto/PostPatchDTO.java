package hr.prism.board.dto;

import hr.prism.board.enums.ExistingRelation;
import hr.prism.board.enums.MemberCategory;
import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.ExceptionCode;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.URL;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class PostPatchDTO extends ResourcePatchDTO {

    private Optional<String> description;

    @Size(min = 3, max = 255)
    private Optional<String> organizationName;

    @Valid
    private Optional<LocationDTO> location;

    @URL
    private Optional<String> applyWebsite;

    @Valid
    private Optional<DocumentDTO> applyDocument;

    @Email
    private Optional<String> applyEmail;

    private Optional<List<String>> postCategories;

    private Optional<List<MemberCategory>> memberCategories;

    private Optional<ExistingRelation> existingRelation;

    private Optional<LinkedHashMap<String, Object>> existingRelationExplanation;

    private Optional<LocalDateTime> liveTimestamp;

    private Optional<LocalDateTime> deadTimestamp;

    private String comment;

    public Optional<String> getDescription() {
        return description;
    }

    public PostPatchDTO setDescription(Optional<String> description) {
        this.description = description;
        return this;
    }

    public Optional<String> getOrganizationName() {
        return organizationName;
    }

    public PostPatchDTO setOrganizationName(Optional<String> organizationName) {
        this.organizationName = organizationName;
        return this;
    }

    public Optional<LocationDTO> getLocation() {
        return location;
    }

    public PostPatchDTO setLocation(Optional<LocationDTO> location) {
        this.location = location;
        return this;
    }

    public Optional<String> getApplyWebsite() {
        return applyWebsite;
    }

    public PostPatchDTO setApplyWebsite(Optional<String> applyWebsite) {
        this.applyWebsite = applyWebsite;
        return this;
    }

    public Optional<DocumentDTO> getApplyDocument() {
        return applyDocument;
    }

    public PostPatchDTO setApplyDocument(Optional<DocumentDTO> applyDocument) {
        this.applyDocument = applyDocument;
        return this;
    }

    public Optional<String> getApplyEmail() {
        return applyEmail;
    }

    public PostPatchDTO setApplyEmail(Optional<String> applyEmail) {
        this.applyEmail = applyEmail;
        return this;
    }

    public Optional<ExistingRelation> getExistingRelation() {
        return existingRelation;
    }

    public PostPatchDTO setExistingRelation(Optional<ExistingRelation> existingRelation) {
        this.existingRelation = existingRelation;
        return this;
    }

    public Optional<LinkedHashMap<String, Object>> getExistingRelationExplanation() {
        return existingRelationExplanation;
    }

    public PostPatchDTO setExistingRelationExplanation(Optional<LinkedHashMap<String, Object>> existingRelationExplanation) {
        this.existingRelationExplanation = existingRelationExplanation;
        return this;
    }

    public Optional<List<String>> getPostCategories() {
        return postCategories;
    }

    public PostPatchDTO setPostCategories(Optional<List<String>> postCategories) {
        this.postCategories = postCategories;
        return this;
    }

    public Optional<List<MemberCategory>> getMemberCategories() {
        return memberCategories;
    }

    public PostPatchDTO setMemberCategories(Optional<List<MemberCategory>> memberCategories) {
        this.memberCategories = memberCategories;
        return this;
    }

    public Optional<LocalDateTime> getLiveTimestamp() {
        return liveTimestamp;
    }

    public PostPatchDTO setLiveTimestamp(Optional<LocalDateTime> liveTimestamp) {
        this.liveTimestamp = liveTimestamp;
        return this;
    }

    public Optional<LocalDateTime> getDeadTimestamp() {
        return deadTimestamp;
    }

    public PostPatchDTO setDeadTimestamp(Optional<LocalDateTime> deadTimestamp) {
        this.deadTimestamp = deadTimestamp;
        return this;
    }

    public String getComment() {
        return comment;
    }

    public PostPatchDTO setComment(String comment) {
        this.comment = comment;
        return this;
    }

    public boolean hasUpdates() {
        for (Field field : getClass().getDeclaredFields()) {
            field.setAccessible(true);
            if (!field.getName().equals("comment")) {
                try {
                    if (field.get(this) != null) {
                        return true;
                    }
                } catch (IllegalAccessException e) {
                    throw new BoardException(ExceptionCode.PROBLEM, e);
                }
            }
        }

        return false;
    }

}
