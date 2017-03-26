package hr.prism.board.representation;

import hr.prism.board.domain.Role;

import java.util.List;

public class PostRepresentation {
    
    private Long id;
    
    private String name;
    
    private String description;
    
    private String organizationName;
    
    private LocationRepresentation location;
    
    private Boolean existingRelation;
    
    private String existingRelationDescription;
    
    private List<String> postCategories;
    
    private List<String> memberCategories;
    
    private String applyWebsite;
    
    private DocumentRepresentation applyDocument;
    
    private String applyEmail;
    
    private BoardRepresentation board;
    
    private List<Role> roles;
    
    public Long getId() {
        return id;
    }
    
    public PostRepresentation setId(Long id) {
        this.id = id;
        return this;
    }
    
    public String getName() {
        return name;
    }
    
    public PostRepresentation setName(String name) {
        this.name = name;
        return this;
    }
    
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
    
    public Boolean getExistingRelation() {
        return existingRelation;
    }
    
    public PostRepresentation setExistingRelation(Boolean existingRelation) {
        this.existingRelation = existingRelation;
        return this;
    }
    
    public String getExistingRelationDescription() {
        return existingRelationDescription;
    }
    
    public PostRepresentation setExistingRelationDescription(String existingRelationDescription) {
        this.existingRelationDescription = existingRelationDescription;
        return this;
    }
    
    public List<String> getPostCategories() {
        return postCategories;
    }
    
    public PostRepresentation setPostCategories(List<String> postCategories) {
        this.postCategories = postCategories;
        return this;
    }
    
    public List<String> getMemberCategories() {
        return memberCategories;
    }
    
    public PostRepresentation setMemberCategories(List<String> memberCategories) {
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
    
    public List<Role> getRoles() {
        return roles;
    }
    
    public PostRepresentation setRoles(List<Role> roles) {
        this.roles = roles;
        return this;
    }
    
}
