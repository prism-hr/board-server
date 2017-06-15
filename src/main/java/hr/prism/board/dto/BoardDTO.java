package hr.prism.board.dto;

import javax.validation.Valid;
import java.util.List;

public class BoardDTO extends ResourceDTO {

    @Valid
    private DocumentDTO documentLogo;

    private List<String> postCategories;

    @Valid
    private DepartmentDTO department;

    public DocumentDTO getDocumentLogo() {
        return documentLogo;
    }

    public BoardDTO setDocumentLogo(DocumentDTO documentLogo) {
        this.documentLogo = documentLogo;
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
