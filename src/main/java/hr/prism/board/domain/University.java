package hr.prism.board.domain;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import static hr.prism.board.enums.Scope.Value.UNIVERSITY;

@Entity
@DiscriminatorValue(value = UNIVERSITY)
public class University extends Resource {

    @Column(name = "homepage")
    private String homepage;

    public String getHomepage() {
        return homepage;
    }

    public void setHomepage(String homepage) {
        this.homepage = homepage;
    }

}
