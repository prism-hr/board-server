package hr.prism.board.domain;

import hr.prism.board.enums.Scope;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = Scope.Value.UNIVERSITY)
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
