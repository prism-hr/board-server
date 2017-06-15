package hr.prism.board.dto;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class DepartmentPatchDTO extends ResourcePatchDTO {

    @Valid
    private Optional<DocumentDTO> documentLogo;

    @Size(min = 1, max = 25)
    @Pattern(regexp = "^[a-z0-9-]+$")
    private Optional<String> handle;

    private Optional<List<String>> memberCategories;

    public Optional<DocumentDTO> getDocumentLogo() {
        return documentLogo;
    }

    public DepartmentPatchDTO setDocumentLogo(Optional<DocumentDTO> documentLogo) {
        this.documentLogo = documentLogo;
        return this;
    }

    public Optional<String> getHandle() {
        return handle;
    }

    public DepartmentPatchDTO setHandle(Optional<String> handle) {
        this.handle = handle;
        return this;
    }

    public Optional<List<String>> getMemberCategories() {
        return memberCategories;
    }

    public DepartmentPatchDTO setMemberCategories(Optional<List<String>> memberCategories) {
        this.memberCategories = memberCategories;
        return this;
    }

}
