package hr.prism.board.repository.dto;

public class BoardDepartmentDTO {
    
    private Long boardId;
    
    private Long id;
    
    private String name;
    
    private String documentLogoCloudinaryId;
    
    private String documentLogoCloudinaryUrl;
    
    private String documentFileName;
    
    private String handle;
    
    private String memberCategories;
    
    public Long getBoardId() {
        return boardId;
    }
    
    public BoardDepartmentDTO setBoardId(Long boardId) {
        this.boardId = boardId;
        return this;
    }
    
    public Long getId() {
        return id;
    }
    
    public BoardDepartmentDTO setId(Long id) {
        this.id = id;
        return this;
    }
    
    public String getName() {
        return name;
    }
    
    public BoardDepartmentDTO setName(String name) {
        this.name = name;
        return this;
    }
    
    public String getDocumentLogoCloudinaryId() {
        return documentLogoCloudinaryId;
    }
    
    public BoardDepartmentDTO setDocumentLogoCloudinaryId(String documentLogoCloudinaryId) {
        this.documentLogoCloudinaryId = documentLogoCloudinaryId;
        return this;
    }
    
    public String getDocumentLogoCloudinaryUrl() {
        return documentLogoCloudinaryUrl;
    }
    
    public BoardDepartmentDTO setDocumentLogoCloudinaryUrl(String documentLogoCloudinaryUrl) {
        this.documentLogoCloudinaryUrl = documentLogoCloudinaryUrl;
        return this;
    }
    
    public String getDocumentFileName() {
        return documentFileName;
    }
    
    public BoardDepartmentDTO setDocumentFileName(String documentFileName) {
        this.documentFileName = documentFileName;
        return this;
    }
    
    public String getHandle() {
        return handle;
    }
    
    public BoardDepartmentDTO setHandle(String handle) {
        this.handle = handle;
        return this;
    }
    
    public String getMemberCategories() {
        return memberCategories;
    }
    
    public BoardDepartmentDTO setMemberCategories(String memberCategories) {
        this.memberCategories = memberCategories;
        return this;
    }
    
}
