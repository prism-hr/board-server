package hr.prism.board.domain;

import hr.prism.board.enums.Scope;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;

@Entity
@DiscriminatorValue(value = Scope.Value.DEPARTMENT)
@NamedEntityGraph(
    name = "department.extended",
    attributeNodes = {
        @NamedAttributeNode(value = "categories"),
        @NamedAttributeNode(value = "documentLogo")})
public class Department extends Resource {

}
