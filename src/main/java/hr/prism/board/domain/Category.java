package hr.prism.board.domain;

import hr.prism.board.enums.CategoryType;

import javax.persistence.*;

@Entity
@Table(name = "category")
public class Category extends BoardEntity {

    @ManyToOne
    @JoinColumn(name = "parent_resource_id", nullable = false)
    private Resource parentResource;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "type", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private CategoryType type;

    @Column(name = "active", nullable = false)
    private boolean active;

    public Resource getParentResource() {
        return parentResource;
    }

    public Category setParentResource(Resource parentResource) {
        this.parentResource = parentResource;
        return this;
    }

    public String getName() {
        return name;
    }

    public Category setName(String name) {
        this.name = name;
        return this;
    }

    public CategoryType getType() {
        return type;
    }

    public Category setType(CategoryType type) {
        this.type = type;
        return this;
    }

    public boolean isActive() {
        return active;
    }

    public Category setActive(boolean active) {
        this.active = active;
        return this;
    }
    
}
