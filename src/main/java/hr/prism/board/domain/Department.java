package hr.prism.board.domain;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = Scope.Value.DEPARTMENT)
public class Department extends Resource {
    
}
