package hr.prism.board.dto;

import hr.prism.board.enums.PostVisibility;

import java.util.List;

public class BoardSettingsDTO {

    private List<String> postCategories;

    private PostVisibility defaultPostVisibility;

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
