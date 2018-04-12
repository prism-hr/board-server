package hr.prism.board.dto;

import hr.prism.board.enums.MemberCategory;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Optional;

public class DepartmentPatchDTO extends ResourcePatchDTO<DepartmentPatchDTO> {

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<String> summary;

    @Valid
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<DocumentDTO> documentLogo;

    @Size(min = 1, max = 25)
    @Pattern(regexp = "^[a-z0-9-]+$")
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<String> handle;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<List<MemberCategory>> memberCategories;

    public Optional<String> getSummary() {
        return summary;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public DepartmentPatchDTO setSummary(Optional<String> summary) {
        this.summary = summary;
        return this;
    }

    public Optional<DocumentDTO> getDocumentLogo() {
        return documentLogo;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public DepartmentPatchDTO setDocumentLogo(Optional<DocumentDTO> documentLogo) {
        this.documentLogo = documentLogo;
        return this;
    }

    public Optional<String> getHandle() {
        return handle;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public DepartmentPatchDTO setHandle(Optional<String> handle) {
        this.handle = handle;
        return this;
    }

    public Optional<List<MemberCategory>> getMemberCategories() {
        return memberCategories;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public DepartmentPatchDTO setMemberCategories(Optional<List<MemberCategory>> memberCategories) {
        this.memberCategories = memberCategories;
        return this;
    }

}
