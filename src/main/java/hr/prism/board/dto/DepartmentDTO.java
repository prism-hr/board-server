package hr.prism.board.dto;

import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;

public class DepartmentDTO extends ResourceDTO<DepartmentDTO> {

    @Valid
    private DocumentDTO documentLogo;

    @NotEmpty
    @Size(max = 15)
    @Pattern(regexp = "^[a-z0-9-]+$")
    private String handle;

    private List<String> memberCategories;

    public DocumentDTO getDocumentLogo() {
        return documentLogo;
    }

    public DepartmentDTO setDocumentLogo(DocumentDTO documentLogo) {
        this.documentLogo = documentLogo;
        return this;
    }

    public String getHandle() {
        return handle;
    }

    public DepartmentDTO setHandle(String handle) {
        this.handle = handle;
        return this;
    }

    public List<String> getMemberCategories() {
        return memberCategories;
    }

    public DepartmentDTO setMemberCategories(List<String> memberCategories) {
        this.memberCategories = memberCategories;
        return this;
    }
}
