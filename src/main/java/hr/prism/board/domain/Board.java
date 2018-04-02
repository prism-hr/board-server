package hr.prism.board.domain;

import hr.prism.board.enums.Scope;

import javax.persistence.*;

@Entity
@DiscriminatorValue(value = Scope.Value.BOARD)
@NamedEntityGraph(
    name = "board.extended",
    attributeNodes = {
        @NamedAttributeNode(value = "parent", subgraph = "department"),
        @NamedAttributeNode(value = "categories")},
    subgraphs = {
        @NamedSubgraph(
            name = "department",
            attributeNodes = {
                @NamedAttributeNode(value = "parent", subgraph = "university"),
                @NamedAttributeNode(value = "documentLogo")}),
        @NamedSubgraph(
            name = "university",
            attributeNodes = {
                @NamedAttributeNode(value = "documentLogo")})})
public class Board extends Resource {

    @Override
    public void setDocumentLogo(Document documentLogo) {
    }

}
