package hr.prism.board.dto;

import hr.prism.board.enums.BoardType;

import javax.validation.Valid;
import java.util.List;

public class BoardDTO extends ResourceDTO<BoardDTO> {

    private BoardType type;

    @Valid
    private DocumentDTO documentLogo;

    private List<String> postCategories;

    public BoardType getType() {
        return type;
    }

    public BoardDTO setType(BoardType type) {
        this.type = type;
        return this;
    }

    public DocumentDTO getDocumentLogo() {
        return documentLogo;
    }

    public BoardDTO setDocumentLogo(DocumentDTO documentLogo) {
        this.documentLogo = documentLogo;
        return this;
    }

    public List<String> getPostCategories() {
        return postCategories;
    }

    public BoardDTO setPostCategories(List<String> postCategories) {
        this.postCategories = postCategories;
        return this;
    }

}
