package hr.prism.board.domain;

import hr.prism.board.enums.BoardType;
import hr.prism.board.enums.Scope;

import javax.persistence.*;

@Entity
@DiscriminatorValue(value = Scope.Value.BOARD)
@NamedEntityGraph(
    name = "board.extended",
    attributeNodes = {
        @NamedAttributeNode(value = "parent", subgraph = "department"),
        @NamedAttributeNode(value = "documentLogo"),
        @NamedAttributeNode(value = "categories")},
    subgraphs = {
        @NamedSubgraph(
            name = "department",
            attributeNodes = {
                @NamedAttributeNode(value = "parent", subgraph = "university"),
                @NamedAttributeNode(value = "documentLogo"),
                @NamedAttributeNode(value = "categories"),
                @NamedAttributeNode(value = "tasks")}),
        @NamedSubgraph(
            name = "university",
            attributeNodes = {
                @NamedAttributeNode(value = "documentLogo")})})
public class Board extends Resource {

    @Enumerated(EnumType.STRING)
    @Column(name = "board_type")
    private BoardType type;

    @Column(name = "post_count")
    private Long postCount;

    public Long getPostCount() {
        return postCount;
    }

    public void setPostCount(Long postCount) {
        this.postCount = postCount;
    }

    public BoardType getType() {
        return type;
    }

    public void setType(BoardType type) {
        this.type = type;
    }

}
