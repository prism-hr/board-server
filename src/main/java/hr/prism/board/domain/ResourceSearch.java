package hr.prism.board.domain;

import javax.persistence.*;

@Entity
@Table(name = "resource_search", uniqueConstraints = @UniqueConstraint(columnNames = {"resource_id", "search"}))
public class ResourceSearch extends BoardEntity {

    @ManyToOne
    @JoinColumn(name = "resource_id", nullable = false)
    private Resource resource;

    @Column(name = "search", nullable = false)
    private String search;

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

}
