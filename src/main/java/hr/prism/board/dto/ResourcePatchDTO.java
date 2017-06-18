package hr.prism.board.dto;

import javax.validation.constraints.Size;
import java.util.Optional;

public class ResourcePatchDTO {

    @Size(min = 3, max = 100)
    private Optional<String> name;

    @Size(min = 3, max = 1000)
    private Optional<String> summary;

    private String comment;

    public Optional<String> getName() {
        return name;
    }

    public ResourcePatchDTO setName(Optional<String> name) {
        this.name = name;
        return this;
    }

    public Optional<String> getSummary() {
        return summary;
    }

    public ResourcePatchDTO setSummary(Optional<String> summary) {
        this.summary = summary;
        return this;
    }

    public String getComment() {
        return comment;
    }

    public ResourcePatchDTO setComment(String comment) {
        this.comment = comment;
        return this;
    }

}
