package hr.prism.board.domain;

import hr.prism.board.enums.CategoryType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;

import static javax.persistence.EnumType.STRING;

@Entity
@Table(name = "resource_category", uniqueConstraints = @UniqueConstraint(columnNames = {"resource_id", "type", "name"}))
public class ResourceCategory extends BoardEntity {

    @ManyToOne
    @JoinColumn(name = "resource_id", nullable = false)
    private Resource resource;

    @Enumerated(STRING)
    @Column(name = "type", nullable = false)
    private CategoryType type;

    @Column(name = "name", nullable = false)
    private String name;

    public Resource getResource() {
        return resource;
    }

    public ResourceCategory setResource(Resource resource) {
        this.resource = resource;
        return this;
    }

    public CategoryType getType() {
        return type;
    }

    public ResourceCategory setType(CategoryType type) {
        this.type = type;
        return this;
    }

    public String getName() {
        return name;
    }

    public ResourceCategory setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(resource)
            .append(name)
            .append(type)
            .toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        ResourceCategory that = (ResourceCategory) other;
        return new EqualsBuilder()
            .append(resource, that.resource)
            .append(name, that.name)
            .append(type, that.type)
            .isEquals();
    }

}
