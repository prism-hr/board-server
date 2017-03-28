package hr.prism.board.dto;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.util.List;

public class PostDTO extends ResourceDTO<PostDTO> {

    @NotEmpty
    @Size(max = 2000)
    private String description;

    @NotEmpty
    @Size(max = 255)
    private String organizationName;

    @Valid
    private LocationDTO location;

    private String existingRelation;

    private List<String> postCategories;

    private List<String> memberCategories;

    @Size(max = 255)
    private String applyWebsite;

    @Valid
    private DocumentDTO applyDocument;

    @Email
    private String applyEmail;

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

    public String getExistingRelation() {
        return existingRelation;
    }

    public PostDTO setExistingRelation(String existingRelation) {
        this.existingRelation = existingRelation;
        return this;
    }

    public List<String> getPostCategories() {
        return postCategories;
    }

    public PostDTO setPostCategories(List<String> postCategories) {
        this.postCategories = postCategories;
        return this;
    }

    public List<String> getMemberCategories() {
        return memberCategories;
    }

    public PostDTO setMemberCategories(List<String> memberCategories) {
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
}
