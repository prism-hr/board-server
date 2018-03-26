package hr.prism.board.dto;

import java.util.List;

public class BoardDTO extends ResourceDTO<BoardDTO> {

    private List<String> postCategories;

    public List<String> getPostCategories() {
        return postCategories;
    }

    public BoardDTO setPostCategories(List<String> postCategories) {
        this.postCategories = postCategories;
        return this;
    }

}
