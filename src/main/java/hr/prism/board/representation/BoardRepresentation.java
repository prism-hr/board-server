package hr.prism.board.representation;

import java.util.List;

public class BoardRepresentation extends ResourceRepresentation<BoardRepresentation> {

    private String handle;

    private DepartmentRepresentation department;

    private List<String> postCategories;

    public String getHandle() {
        return handle;
    }

    public BoardRepresentation setHandle(String handle) {
        this.handle = handle;
        return this;
    }

    public DepartmentRepresentation getDepartment() {
        return department;
    }

    public BoardRepresentation setDepartment(DepartmentRepresentation department) {
        this.department = department;
        return this;
    }

    public List<String> getPostCategories() {
        return postCategories;
    }

    public BoardRepresentation setPostCategories(List<String> postCategories) {
        this.postCategories = postCategories;
        return this;
    }

}
