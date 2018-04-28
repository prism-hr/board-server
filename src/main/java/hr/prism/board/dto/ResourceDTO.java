package hr.prism.board.dto;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.validation.constraints.Size;

public class ResourceDTO<T extends ResourceDTO> {

    @Size(min = 3, max = 100)
    private String name;

    public String getName() {
        return name;
    }

    @SuppressWarnings("unchecked")
    public T setName(String name) {
        this.name = name;
        return (T) this;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(name)
            .toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        ResourceDTO<?> that = (ResourceDTO<?>) other;
        return new EqualsBuilder()
            .append(name, that.name)
            .isEquals();
    }

}
