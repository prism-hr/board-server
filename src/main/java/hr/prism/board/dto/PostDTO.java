package hr.prism.board.dto;

import hr.prism.board.enums.ExistingRelation;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.URL;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;

public class PostDTO {

    @NotEmpty
    @Size(max = 255)
    private String name;

    @NotEmpty
    @Size(max = 2000)
    private String description;

    @NotEmpty
    @Size(max = 255)
    private String organizationName;

    @Valid
    private LocationDTO location;

    private ExistingRelation existingRelation;

    private LinkedHashMap<String, Object> existingRelationExplanation;

    private List<String> postCategories;

    private List<String> memberCategories;
    
    @URL
    private String applyWebsite;

    @Valid
    private DocumentDTO applyDocument;
    
    @Email
    private String applyEmail;

    private LocalDateTime liveTimestamp;

    private LocalDateTime deadTimestamp;

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

    public ExistingRelation getExistingRelation() {
        return existingRelation;
    }

    public PostDTO setExistingRelation(ExistingRelation existingRelation) {
        this.existingRelation = existingRelation;
        return this;
    }

    public LinkedHashMap<String, Object> getExistingRelationExplanation() {
        return existingRelationExplanation;
    }

    public PostDTO setExistingRelationExplanation(LinkedHashMap<String, Object> existingRelationExplanation) {
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
