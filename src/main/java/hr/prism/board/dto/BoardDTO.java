package hr.prism.board.dto;

import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.util.List;

public class BoardDTO {

    @NotEmpty
    @Size(max = 255)
    private String name;

    @NotEmpty
    @Size(max = 2000)
    private String purpose;

    private List<String> postCategories;

    @Valid
    private DepartmentDTO department;

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

    public List<String> getPostCategories() {
        return postCategories;
    }

    public BoardDTO setPostCategories(List<String> postCategories) {
        this.postCategories = postCategories;
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
