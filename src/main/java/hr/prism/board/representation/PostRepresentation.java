package hr.prism.board.representation;

import java.util.List;

public class PostRepresentation extends AbstractResourceRepresentation {
    
    private String description;
    
    private String organizationName;
    
    private LocationRepresentation location;
    
    private String existingRelation;
    
    private List<String> postCategories;
    
    private List<String> memberCategories;
    
    private String applyWebsite;
    
    private DocumentRepresentation applyDocument;
    
    private String applyEmail;
    
    private BoardRepresentation board;
    
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
    
    public String getExistingRelation() {
        return existingRelation;
    }
    
    public PostRepresentation setExistingRelation(String existingRelation) {
        this.existingRelation = existingRelation;
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
    
}
