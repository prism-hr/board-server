package hr.prism.board.dto;

import javax.validation.constraints.Size;
import java.util.Optional;

@SuppressWarnings("unchecked")
public class ResourcePatchDTO<T extends ResourcePatchDTO> {

    @Size(min = 3, max = 100)
    private Optional<String> name;

    @Size(min = 3, max = 1000)
    private Optional<String> summary;

    private String comment;

    public Optional<String> getName() {
        return name;
    }

    public T setName(Optional<String> name) {
        this.name = name;
        return (T) this;
    }

    public Optional<String> getSummary() {
        return summary;
    }

    public T setSummary(Optional<String> summary) {
        this.summary = summary;
        return (T) this;
    }

    public String getComment() {
        return comment;
    }

    public T setComment(String comment) {
        this.comment = comment;
        return (T) this;
    }

}
