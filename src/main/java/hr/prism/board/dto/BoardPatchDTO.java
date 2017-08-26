package hr.prism.board.dto;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class BoardPatchDTO extends ResourcePatchDTO<BoardPatchDTO> {

    @Valid
    private Optional<DocumentDTO> documentLogo;

    @Size(min = 1, max = 25)
    @Pattern(regexp = "^[a-z0-9-]+$")
    private Optional<String> handle;

    private Optional<List<String>> postCategories;

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

}
