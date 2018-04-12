package hr.prism.board.dto;

import javax.validation.constraints.Size;
import java.util.Optional;

@SuppressWarnings("unchecked")
public class ResourcePatchDTO<T extends ResourcePatchDTO> {

    @Size(min = 3, max = 100)
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<String> name;

    private String comment;

    public Optional<String> getName() {
        return name;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public T setName(Optional<String> name) {
        this.name = name;
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
