package hr.prism.board.dto;

import javax.validation.constraints.NotNull;

public class UniversityDTO extends ResourceDTO<UniversityDTO> {

    @NotNull
    private Long id;

    public Long getId() {
        return id;
    }

    public UniversityDTO setId(Long id) {
        this.id = id;
        return this;
    }

}
