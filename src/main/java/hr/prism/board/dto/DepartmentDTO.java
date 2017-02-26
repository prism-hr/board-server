package hr.prism.board.dto;

import javax.validation.Valid;

public class DepartmentDTO {

    private Long id;

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
}
