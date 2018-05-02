package hr.prism.board.dto;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

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

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .appendSuper(super.hashCode())
            .append(postCategories)
            .toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        BoardDTO boardDTO = (BoardDTO) other;
        return new EqualsBuilder()
            .appendSuper(super.equals(other))
            .append(postCategories, boardDTO.postCategories)
            .isEquals();
    }

}
