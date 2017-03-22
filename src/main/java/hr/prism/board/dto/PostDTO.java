package hr.prism.board.dto;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class PostDTO {

    private Long id;

    @NotEmpty
    private String name;

    @NotEmpty
    @Size(max = 2000)
    private String description;

    @NotEmpty
    @Size(max = 255)
    private String organizationName;

    @Valid
    private LocationDTO location;

    @NotNull
    private Boolean existingRelation;

    @Size(max = 255)
    private String applyWebsite;

    @Valid
    private DocumentDTO applyDocument;

    @Email
    private String applyEmail;

    public Long getId() {
        return id;
    }

    public PostDTO setId(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public PostDTO setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public PostDTO setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public PostDTO setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
        return this;
    }

    public LocationDTO getLocation() {
        return location;
    }

    public PostDTO setLocation(LocationDTO location) {
        this.location = location;
        return this;
    }

    public Boolean getExistingRelation() {
        return existingRelation;
    }

    public PostDTO setExistingRelation(Boolean existingRelation) {
        this.existingRelation = existingRelation;
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
}
