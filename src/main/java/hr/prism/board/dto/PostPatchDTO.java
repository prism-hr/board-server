package hr.prism.board.dto;

import org.hibernate.validator.constraints.Email;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Optional;

public class PostPatchDTO {

    @Size(min=1, max = 255)
    private Optional<String> name;

    @Size(min=1, max = 2000)
    private Optional<String> description;

    @Size(min=1, max = 255)
    private Optional<String> organizationName;

    @Valid
    private Optional<LocationDTO> location;

    private Optional<List<String>> postCategories;

    private Optional<List<String>> memberCategories;

    @Size(min=1, max = 255)
    private Optional<String> applyWebsite;

    @Valid
    private Optional<DocumentDTO> applyDocument;

    @Email
    private Optional<String> applyEmail;

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
}
