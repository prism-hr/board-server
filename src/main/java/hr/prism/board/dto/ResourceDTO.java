package hr.prism.board.dto;

import javax.validation.constraints.Size;

public class ResourceDTO {

    @Size(min = 3, max = 100)
    private String name;

    @Size(min = 3, max = 1000)
    private String summary;

    public String getName() {
        return name;
    }

    public ResourceDTO setName(String name) {
        this.name = name;
        return this;
    }

    public String getSummary() {
        return summary;
    }

    public ResourceDTO setSummary(String summary) {
        this.summary = summary;
        return this;
    }

}
