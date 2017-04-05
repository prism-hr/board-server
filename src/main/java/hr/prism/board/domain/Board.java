package hr.prism.board.domain;

import hr.prism.board.enums.PostVisibility;

import javax.persistence.*;

@Entity
@DiscriminatorValue(value = Scope.Value.BOARD)
@NamedEntityGraph(
    name = "board.extended",
    attributeNodes = {
        @NamedAttributeNode(value = "parent"),
        @NamedAttributeNode(value = "categories"),
        @NamedAttributeNode(value = "documentLogo")})
public class Board extends Resource {
    
    @Column(name = "default_post_visibility")
    @Enumerated(value = EnumType.STRING)
    private PostVisibility defaultPostVisibility;
    
    public PostVisibility getDefaultPostVisibility() {
        return defaultPostVisibility;
    }
    
    public Board setDefaultPostVisibility(PostVisibility defaultPostVisibility) {
        this.defaultPostVisibility = defaultPostVisibility;
        return this;
    }
    
}
