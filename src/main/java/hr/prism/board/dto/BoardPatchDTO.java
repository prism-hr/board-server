package hr.prism.board.dto;

import hr.prism.board.enums.PostVisibility;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class BoardPatchDTO {

    @Size(min = 3, max = 100)
    private Optional<String> name;

    @Size(min = 3, max = 1000)
    private Optional<String> summary;

    @Valid
    private Optional<DocumentDTO> documentLogo;

    @Size(min = 1, max = 25)
    @Pattern(regexp = "^[a-z0-9-]+$")
    private Optional<String> handle;

    private Optional<List<String>> postCategories;

    private Optional<PostVisibility> defaultPostVisibility;

    public Optional<String> getName() {
        return name;
    }

    public BoardPatchDTO setName(Optional<String> name) {
        this.name = name;
        return this;
    }

    public Optional<String> getSummary() {
        return summary;
    }

    public BoardPatchDTO setSummary(Optional<String> summary) {
        this.summary = summary;
        return this;
    }

    public Optional<DocumentDTO> getDocumentLogo() {
        return documentLogo;
    }

    public BoardPatchDTO setDocumentLogo(Optional<DocumentDTO> documentLogo) {
        this.documentLogo = documentLogo;
        return this;
    }

    public Optional<String> getHandle() {
        return handle;
    }

    public BoardPatchDTO setHandle(Optional<String> handle) {
        this.handle = handle;
        return this;
    }

    public Optional<List<String>> getPostCategories() {
        return postCategories;
    }

    public BoardPatchDTO setPostCategories(Optional<List<String>> postCategories) {
        this.postCategories = postCategories;
        return this;
    }

    public Optional<PostVisibility> getDefaultPostVisibility() {
        return defaultPostVisibility;
    }

    public BoardPatchDTO setDefaultPostVisibility(Optional<PostVisibility> defaultPostVisibility) {
        this.defaultPostVisibility = defaultPostVisibility;
        return this;
    }

}
