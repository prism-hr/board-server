package hr.prism.board.dto;

import java.util.List;

public class BoardSettingsDTO {

    private List<String> postCategories;

    public List<String> getPostCategories() {
        return postCategories;
    }

    public void setPostCategories(List<String> postCategories) {
        this.postCategories = postCategories;
    }
}
