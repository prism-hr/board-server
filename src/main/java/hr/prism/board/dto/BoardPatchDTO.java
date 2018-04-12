package hr.prism.board.dto;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class BoardPatchDTO extends ResourcePatchDTO<BoardPatchDTO> {

    @Size(min = 1, max = 25)
    @Pattern(regexp = "^[a-z0-9-]+$")
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<String> handle;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<List<String>> postCategories;

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
