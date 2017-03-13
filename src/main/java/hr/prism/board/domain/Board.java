package hr.prism.board.domain;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "BOARD")
public class Board extends Resource {
    
}
