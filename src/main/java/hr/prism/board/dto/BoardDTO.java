package hr.prism.board.dto;

import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.Size;

public class BoardDTO extends ResourceDTO<BoardDTO> {

    @NotEmpty
    @Size(max = 2000)
    private String purpose;

    @Valid
    private DepartmentDTO department;

    @Valid
    private BoardSettingsDTO settings;

    public String getPurpose() {
        return purpose;
    }

    public BoardDTO setPurpose(String purpose) {
        this.purpose = purpose;
        return this;
    }

    public DepartmentDTO getDepartment() {
        return department;
    }

    public BoardDTO setDepartment(DepartmentDTO department) {
        this.department = department;
        return this;
    }

    public BoardSettingsDTO getSettings() {
        return settings;
    }

    public BoardDTO setSettings(BoardSettingsDTO settings) {
        this.settings = settings;
        return this;
    }
}
