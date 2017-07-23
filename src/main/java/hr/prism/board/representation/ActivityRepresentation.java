package hr.prism.board.representation;

import hr.prism.board.enums.Activity;

public class ActivityRepresentation {

    private Long id;

    private ResourceRepresentation resource;

    private ResourceRepresentation parentResource;

    private UserRoleRepresentation userRole;

    private Activity category;

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

    public ResourceRepresentation getParentResource() {
        return parentResource;
    }

    public ActivityRepresentation setParentResource(ResourceRepresentation parentResource) {
        this.parentResource = parentResource;
        return this;
    }

    public UserRoleRepresentation getUserRole() {
        return userRole;
    }

    public ActivityRepresentation setUserRole(UserRoleRepresentation userRole) {
        this.userRole = userRole;
        return this;
    }

    public Activity getCategory() {
        return category;
    }

    public ActivityRepresentation setCategory(Activity category) {
        this.category = category;
        return this;
    }

}
