package hr.prism.board.domain;

import javax.persistence.*;

@Entity
@Table(name = "resource_search", uniqueConstraints = @UniqueConstraint(columnNames = {"resource_id", "search"}))
public class ResourceSearch extends Search {

    @ManyToOne
    @JoinColumn(name = "resource_id", nullable = false)
    private Resource resource;

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

}
