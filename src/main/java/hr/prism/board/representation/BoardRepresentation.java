package hr.prism.board.representation;

public class BoardRepresentation {

    private Long id;

    private String name;

    private String purpose;

    private DepartmentRepresentation department;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public DepartmentRepresentation getDepartment() {
        return department;
    }

    public void setDepartment(DepartmentRepresentation department) {
        this.department = department;
    }

    public BoardRepresentation withId(final Long id) {
        this.id = id;
        return this;
    }

    public BoardRepresentation withName(final String name) {
        this.name = name;
        return this;
    }

    public BoardRepresentation withPurpose(final String purpose) {
        this.purpose = purpose;
        return this;
    }

    public BoardRepresentation withDepartment(final DepartmentRepresentation department) {
        this.department = department;
        return this;
    }
}
