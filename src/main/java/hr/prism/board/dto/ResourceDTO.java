package hr.prism.board.dto;

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

}
