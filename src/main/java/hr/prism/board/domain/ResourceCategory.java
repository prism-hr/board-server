package hr.prism.board.domain;

import hr.prism.board.enums.CategoryType;

import javax.persistence.*;

@Entity
@Table(name = "resource_category")
public class ResourceCategory extends BoardEntity {

    @ManyToOne
    @JoinColumn(name = "resource_id", nullable = false)
    private Resource resource;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "type", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private CategoryType type;

    @Column(name = "ordinal")
    private Integer ordinal;

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

    public Integer getOrdinal() {
        return ordinal;
    }

    public ResourceCategory setOrdinal(Integer ordinal) {
        this.ordinal = ordinal;
        return this;
    }

}
