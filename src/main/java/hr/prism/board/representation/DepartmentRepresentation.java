package hr.prism.board.representation;

import hr.prism.board.domain.Role;

import java.util.List;

public class DepartmentRepresentation {
    
    private Long id;
    
    private String name;
    
    private DocumentRepresentation documentLogo;
    
    private String handle;
    
    private List<BoardRepresentation> boards;
    
    private List<String> memberCategories;
    
    private List<Role> roles;
    
    public Long getId() {
        return id;
    }
    
    public DepartmentRepresentation setId(Long id) {
        this.id = id;
        return this;
    }
    
    public String getName() {
        return name;
    }
    
    public DepartmentRepresentation setName(String name) {
        this.name = name;
        return this;
    }
    
    public DocumentRepresentation getDocumentLogo() {
        return documentLogo;
    }
    
    public DepartmentRepresentation setDocumentLogo(DocumentRepresentation documentLogo) {
        this.documentLogo = documentLogo;
        return this;
    }
    
    public String getHandle() {
        return handle;
    }
    
    public DepartmentRepresentation setHandle(String handle) {
        this.handle = handle;
        return this;
    }
    
    public List<BoardRepresentation> getBoards() {
        return boards;
    }
    
    public DepartmentRepresentation setBoards(List<BoardRepresentation> boards) {
        this.boards = boards;
        return this;
    }
    
    public List<String> getMemberCategories() {
        return memberCategories;
    }
    
    public DepartmentRepresentation setMemberCategories(List<String> memberCategories) {
        this.memberCategories = memberCategories;
        return this;
    }
    
    public List<Role> getRoles() {
        return roles;
    }
    
    public DepartmentRepresentation setRoles(List<Role> roles) {
        this.roles = roles;
        return this;
    }
    
}
