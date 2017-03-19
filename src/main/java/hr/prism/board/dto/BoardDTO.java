package hr.prism.board.dto;

import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;

public class BoardDTO {
    
    private Long id;
    
    @NotEmpty
    private String name;
    
    @NotEmpty
    private String purpose;
    
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
