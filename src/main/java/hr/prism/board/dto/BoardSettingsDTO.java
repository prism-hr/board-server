package hr.prism.board.dto;

import hr.prism.board.enums.PostVisibility;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;

public class BoardSettingsDTO {

    @NotEmpty
    @Size(max = 15)
    @Pattern(regexp = "^[a-z0-9-]+$")
    private String handle;

    private List<String> postCategories;

    private PostVisibility defaultPostVisibility;

    public String getHandle() {
        return handle;
    }

    public BoardSettingsDTO setHandle(String handle) {
        this.handle = handle;
        return this;
    }

    public List<String> getPostCategories() {
        return postCategories;
    }

    public BoardSettingsDTO setPostCategories(List<String> postCategories) {
        this.postCategories = postCategories;
        return this;
    }

    public PostVisibility getDefaultPostVisibility() {
        return defaultPostVisibility;
    }

    public BoardSettingsDTO setDefaultPostVisibility(PostVisibility defaultPostVisibility) {
        this.defaultPostVisibility = defaultPostVisibility;
        return this;
    }
}
