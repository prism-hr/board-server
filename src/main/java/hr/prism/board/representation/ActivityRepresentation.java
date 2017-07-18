package hr.prism.board.representation;

public class ActivityRepresentation {

    private Long id;

    private ResourceRepresentation resource;

    private UserRoleRepresentation userRole;

    public Long getId() {
        return id;
    }

    public ActivityRepresentation setId(Long id) {
        this.id = id;
        return this;
    }

    public ResourceRepresentation getResource() {
        return resource;
    }

    public ActivityRepresentation setResource(ResourceRepresentation resource) {
        this.resource = resource;
        return this;
    }

    public UserRoleRepresentation getUserRole() {
        return userRole;
    }

    public ActivityRepresentation setUserRole(UserRoleRepresentation userRole) {
        this.userRole = userRole;
        return this;
    }

}
