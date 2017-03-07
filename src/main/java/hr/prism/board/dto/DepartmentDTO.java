package hr.prism.board.dto;

import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;

public class DepartmentDTO {

    private Long id;

    @NotEmpty
    private String name;

    @Valid
    private DocumentDTO documentLogo;

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

    public DocumentDTO getDocumentLogo() {
        return documentLogo;
    }

    public void setDocumentLogo(DocumentDTO documentLogo) {
        this.documentLogo = documentLogo;
    }

    public DepartmentDTO withId(final Long id) {
        this.id = id;
        return this;
    }

    public DepartmentDTO withName(final String name) {
        this.name = name;
        return this;
    }

    public DepartmentDTO withDocumentLogo(final DocumentDTO documentLogo) {
        this.documentLogo = documentLogo;
        return this;
    }

}
