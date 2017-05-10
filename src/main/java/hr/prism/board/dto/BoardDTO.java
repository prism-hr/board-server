package hr.prism.board.dto;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.util.List;

public class BoardDTO {

    @Size(min = 3, max = 100)
    private String name;

    @Size(min = 3, max = 1000)
    private String summary;

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

    public String getSummary() {
        return summary;
    }

    public BoardDTO setSummary(String summary) {
        this.summary = summary;
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
