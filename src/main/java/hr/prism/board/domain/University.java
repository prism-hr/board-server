package hr.prism.board.domain;

import hr.prism.board.enums.Scope;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = Scope.Value.UNIVERSITY)
public class University extends Resource {

}
