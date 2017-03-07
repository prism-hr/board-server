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

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public DepartmentDTO getDepartment() {
        return department;
    }

    public void setDepartment(DepartmentDTO department) {
        this.department = department;
    }

    public BoardDTO withId(final Long id) {
        this.id = id;
        return this;
    }

    public BoardDTO withName(final String name) {
        this.name = name;
        return this;
    }

    public BoardDTO withPurpose(final String purpose) {
        this.purpose = purpose;
        return this;
    }

    public BoardDTO withDepartment(final DepartmentDTO department) {
        this.department = department;
        return this;
    }

}
