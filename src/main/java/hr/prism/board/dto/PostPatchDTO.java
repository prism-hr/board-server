package hr.prism.board.dto;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.URL;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class PostPatchDTO extends ResourcePatchDTO {

    @Size(min = 1, max = 255)
    private Optional<String> name;

    @Size(min = 1, max = 2000)
    private Optional<String> description;

    @Size(min = 1, max = 255)
    private Optional<String> organizationName;

    @Valid
    private Optional<LocationDTO> location;

    private Optional<List<String>> postCategories;

    private Optional<List<String>> memberCategories;
    
    @URL
    private Optional<String> applyWebsite;

    @Valid
    private Optional<DocumentDTO> applyDocument;
    
    @Email
    private Optional<String> applyEmail;

    private Optional<LocalDateTime> liveTimestamp;

    private Optional<LocalDateTime> deadTimestamp;

    public Optional<String> getName() {
        return name;
    }

    public PostPatchDTO setName(Optional<String> name) {
        this.name = name;
        return this;
    }

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

    public Optional<List<String>> getPostCategories() {
        return postCategories;
    }

    public PostPatchDTO setPostCategories(Optional<List<String>> postCategories) {
        this.postCategories = postCategories;
        return this;
    }

    public Optional<List<String>> getMemberCategories() {
        return memberCategories;
    }

    public PostPatchDTO setMemberCategories(Optional<List<String>> memberCategories) {
        this.memberCategories = memberCategories;
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

}
