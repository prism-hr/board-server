package hr.prism.board.representation;

public class OrganizationRepresentation<T extends OrganizationRepresentation> {

    private Long id;

    private String name;

    private String logo;

    public Long getId() {
        return id;
    }

    @SuppressWarnings("unchecked")
    public T setId(Long id) {
        this.id = id;
        return (T) this;
    }

    public String getName() {
        return name;
    }

    @SuppressWarnings("unchecked")
    public T setName(String name) {
        this.name = name;
        return (T) this;
    }

    public String getLogo() {
        return logo;
    }

    @SuppressWarnings("unchecked")
    public T setLogo(String logo) {
        this.logo = logo;
        return (T) this;
    }

}
