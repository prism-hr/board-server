package hr.prism.board.dto;

import javax.validation.constraints.NotNull;

public class BoardDTO {

    private Long id;

    @NotNull
    private String name;

    @NotNull
    private String purpose;

    private DepartmentDTO department;

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

}
