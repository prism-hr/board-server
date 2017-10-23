package hr.prism.board.domain;

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

    @Column(name = "post_count")
    private Long postCount;

    @Column(name = "author_count")
    private Long authorCount;

    public Long getPostCount() {
        return postCount;
    }

    public void setPostCount(Long postCount) {
        this.postCount = postCount;
    }

    public Long getAuthorCount() {
        return authorCount;
    }

    public void setAuthorCount(Long authorCount) {
        this.authorCount = authorCount;
    }

}
