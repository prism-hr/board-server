package hr.prism.board.representation;

public class PostRepresentation {

    private Long id;

    private String name;

    private String description;

    private String organizationName;

    private LocationRepresentation location;

    private Boolean existingRelation;

    private String applyWebsite;

    private DocumentRepresentation applyDocument;

    private String applyEmail;

    private BoardRepresentation board;

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
