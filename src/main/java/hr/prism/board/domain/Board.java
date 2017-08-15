package hr.prism.board.domain;

import hr.prism.board.enums.PostVisibility;
import hr.prism.board.enums.Scope;

import javax.persistence.*;

@Entity
@DiscriminatorValue(value = Scope.Value.BOARD)
@NamedEntityGraph(
    name = "board.extended",
    attributeNodes = {
        @NamedAttributeNode(value = "parent", subgraph = "department"),
        @NamedAttributeNode(value = "categories"),
        @NamedAttributeNode(value = "documentLogo")},
    subgraphs = {
        @NamedSubgraph(
            name = "department",
            attributeNodes = {
                @NamedAttributeNode(value = "categories"),
                @NamedAttributeNode(value = "documentLogo")})})
public class Board extends Resource {

    @Column(name = "default_post_visibility")
    @Enumerated(value = EnumType.STRING)
    private PostVisibility defaultPostVisibility;

    public PostVisibility getDefaultPostVisibility() {
        return defaultPostVisibility;
    }

    public void setDefaultPostVisibility(PostVisibility defaultPostVisibility) {
        this.defaultPostVisibility = defaultPostVisibility;
    }

}
