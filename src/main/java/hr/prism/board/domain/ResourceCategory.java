package hr.prism.board.domain;

import hr.prism.board.enums.CategoryType;

import javax.persistence.*;

import static javax.persistence.EnumType.STRING;

@Entity
@Table(name = "resource_category")
public class ResourceCategory extends BoardEntity {

    @ManyToOne
    @JoinColumn(name = "resource_id", nullable = false)
    private Resource resource;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(STRING)
    @Column(name = "type", nullable = false)
    private CategoryType type;

    public Resource getResource() {
        return resource;
    }

    public ResourceCategory setResource(Resource resource) {
        this.resource = resource;
        return this;
    }

    public String getName() {
        return name;
    }

    public ResourceCategory setName(String name) {
        this.name = name;
        return this;
    }

    public CategoryType getType() {
        return type;
    }

    public ResourceCategory setType(CategoryType type) {
        this.type = type;
        return this;
    }

}
