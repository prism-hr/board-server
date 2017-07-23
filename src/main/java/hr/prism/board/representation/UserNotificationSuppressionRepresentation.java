package hr.prism.board.representation;

public class UserNotificationSuppressionRepresentation {

    private ResourceRepresentation resource;

    private Boolean suppressed;

    public ResourceRepresentation getResource() {
        return resource;
    }

    public UserNotificationSuppressionRepresentation setResource(ResourceRepresentation resource) {
        this.resource = resource;
        return this;
    }

    public Boolean getSuppressed() {
        return suppressed;
    }

    public UserNotificationSuppressionRepresentation setSuppressed(Boolean suppressed) {
        this.suppressed = suppressed;
        return this;
    }

}
