package hr.prism.board.representation;

import hr.prism.board.enums.Activity;

public class ActivityRepresentation {

    private Long id;

    private ResourceRepresentation resource;

    private UserRoleRepresentation userRole;

    private ResourceEventRepresentation resourceEvent;

    private Activity activity;

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

    public ResourceEventRepresentation getResourceEvent() {
        return resourceEvent;
    }

    public ActivityRepresentation setResourceEvent(ResourceEventRepresentation resourceEvent) {
        this.resourceEvent = resourceEvent;
        return this;
    }

    public Activity getActivity() {
        return activity;
    }

    public ActivityRepresentation setActivity(Activity activity) {
        this.activity = activity;
        return this;
    }

}
