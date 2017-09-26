package hr.prism.board.representation;

public class UniversityRepresentation extends ResourceRepresentation<UniversityRepresentation> {

    private String handle;

    public String getHandle() {
        return handle;
    }

    public UniversityRepresentation setHandle(String handle) {
        this.handle = handle;
        return this;
    }

}
