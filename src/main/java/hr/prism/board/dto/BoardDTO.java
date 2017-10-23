package hr.prism.board.dto;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

public class BoardDTO extends ResourceDTO<BoardDTO> {

    @Valid
    private DocumentDTO documentLogo;

    @Valid
    @NotNull
    private DepartmentDTO department;

    private List<String> postCategories;

    public DocumentDTO getDocumentLogo() {
        return documentLogo;
    }

    public BoardDTO setDocumentLogo(DocumentDTO documentLogo) {
        this.documentLogo = documentLogo;
        return this;
    }

    public DepartmentDTO getDepartment() {
        return department;
    }

    public BoardDTO setDepartment(DepartmentDTO department) {
        this.department = department;
        return this;
    }

    public List<String> getPostCategories() {
        return postCategories;
    }

    public BoardDTO setPostCategories(List<String> postCategories) {
        this.postCategories = postCategories;
        return this;
    }

}
