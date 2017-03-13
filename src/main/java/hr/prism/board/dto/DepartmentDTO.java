package hr.prism.board.dto;

import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import java.util.List;

public class DepartmentDTO {

    private Long id;

    @NotEmpty
    private String name;

    @Valid
    private DocumentDTO documentLogo;

    private List<String> postCategories;

    public Long getId() {
        return id;
    }

    public DepartmentDTO setId(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public DepartmentDTO setName(String name) {
        this.name = name;
        return this;
    }

    public DocumentDTO getDocumentLogo() {
        return documentLogo;
    }

    public DepartmentDTO setDocumentLogo(DocumentDTO documentLogo) {
        this.documentLogo = documentLogo;
        return this;
    }

    public List<String> getPostCategories() {
        return postCategories;
    }

    public DepartmentDTO setPostCategories(List<String> postCategories) {
        this.postCategories = postCategories;
        return this;
    }
}
