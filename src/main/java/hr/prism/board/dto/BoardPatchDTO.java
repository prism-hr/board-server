package hr.prism.board.dto;

import hr.prism.board.enums.PostVisibility;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Optional;

public class BoardPatchDTO {

    @Size(max = 255)
    private Optional<String> name;

    @Size(max = 2000)
    private Optional<String> purpose;

    @Size(max = 15)
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

    public Optional<String> getPurpose() {
        return purpose;
    }

    public BoardPatchDTO setPurpose(Optional<String> purpose) {
        this.purpose = purpose;
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
