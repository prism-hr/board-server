package hr.prism.board.dto;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class BoardDTO {
    
    private Long id;
    
    @NotNull
    @Size(min = 1)
    private String name;
    
    @NotNull
    @Size(min = 1)
    private String purpose;
    
    @NotNull
    @Size(min = 1, max = 1)
    private String handle;
    
    @Valid
    private DepartmentDTO department;
    
    @Valid
    private BoardSettingsDTO settings;
    
    public Long getId() {
        return id;
    }
    
    public BoardDTO setId(Long id) {
        this.id = id;
        return this;
    }
    
    public String getName() {
        return name;
    }
    
    public BoardDTO setName(String name) {
        this.name = name;
        return this;
    }
    
    public String getPurpose() {
        return purpose;
    }
    
    public BoardDTO setPurpose(String purpose) {
        this.purpose = purpose;
        return this;
    }
    
    public String getHandle() {
        return handle;
    }
    
    public BoardDTO setHandle(String handle) {
        this.handle = handle;
        return this;
    }
    
    public DepartmentDTO getDepartment() {
        return department;
    }
    
    public BoardDTO setDepartment(DepartmentDTO department) {
        this.department = department;
        return this;
    }
    
    public BoardSettingsDTO getSettings() {
        return settings;
    }
    
    public BoardDTO setSettings(BoardSettingsDTO settings) {
        this.settings = settings;
        return this;
    }
}
