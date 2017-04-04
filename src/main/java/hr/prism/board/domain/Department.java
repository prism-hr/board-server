package hr.prism.board.domain;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;

@Entity
@DiscriminatorValue(value = Scope.Value.DEPARTMENT)
@NamedEntityGraph(name = "department.extended",
    attributeNodes = @NamedAttributeNode(value = "categories"))
public class Department extends Resource {
    
}
