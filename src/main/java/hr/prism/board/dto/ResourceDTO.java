package hr.prism.board.dto;

import javax.validation.constraints.Size;

@SuppressWarnings("unchecked")
public class ResourceDTO<T extends ResourceDTO> {

    @Size(min = 3, max = 100)
    private String name;

    public String getName() {
        return name;
    }

    public T setName(String name) {
        this.name = name;
        return (T) this;
    }

}
