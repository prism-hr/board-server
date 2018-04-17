package hr.prism.board.value;

public class OrganizationSearch {

    private Long id;

    private String name;

    private String logo;

    public OrganizationSearch(Long id, String name, String logo) {
        this.id = id;
        this.name = name;
        this.logo = logo;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLogo() {
        return logo;
    }

}
