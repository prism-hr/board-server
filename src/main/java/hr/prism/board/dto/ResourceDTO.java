package hr.prism.board.dto;

import javax.validation.constraints.Size;

@SuppressWarnings("unchecked")
public class ResourceDTO<T extends ResourceDTO> {

    @Size(min = 3, max = 100)
    private String name;

    @Size(min = 3, max = 1000)
    private String summary;

    public String getName() {
        return name;
    }

    public T setName(String name) {
        this.name = name;
        return (T) this;
    }

    public String getSummary() {
        return summary;
    }

    public T setSummary(String summary) {
        this.summary = summary;
        return (T) this;
    }

}
