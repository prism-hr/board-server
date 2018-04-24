package hr.prism.board.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;

@Entity
@Table(name = "resource_relation",
    uniqueConstraints = @UniqueConstraint(columnNames = {"resource1_id", "resource2_id"}))
public class ResourceRelation extends BoardEntity {

    @ManyToOne
    @JoinColumn(name = "resource1_id", nullable = false)
    private Resource resource1;

    @ManyToOne
    @JoinColumn(name = "resource2_id", nullable = false)
    private Resource resource2;

    public Resource getResource1() {
        return resource1;
    }

    public ResourceRelation setResource1(Resource resource1) {
        this.resource1 = resource1;
        return this;
    }

    public Resource getResource2() {
        return resource2;
    }

    public ResourceRelation setResource2(Resource resource2) {
        this.resource2 = resource2;
        return this;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(resource1)
            .append(resource2)
            .toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        ResourceRelation that = (ResourceRelation) other;
        return new EqualsBuilder()
            .append(resource1, that.resource1)
            .append(resource2, that.resource2)
            .isEquals();
    }

}
