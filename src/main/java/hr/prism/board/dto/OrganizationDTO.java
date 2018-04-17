package hr.prism.board.dto;

import javax.validation.constraints.Size;

public class OrganizationDTO {

    private Long id;

    @Size(min = 3, max = 255)
    private String name;

    @Size(min = 3, max = 255)
    private String logo;

    public Long getId() {
        return id;
    }

    public OrganizationDTO setId(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public OrganizationDTO setName(String name) {
        this.name = name;
        return this;
    }

    public String getLogo() {
        return logo;
    }

    public OrganizationDTO setLogo(String logo) {
        this.logo = logo;
        return this;
    }

}
