package hr.prism.board.dto;

public class BoardDTO {

    private Long id;

    private String name;

    private String purpose;

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

    public BoardDTO withId(final Long id) {
        this.id = id;
        return this;
    }

    public BoardDTO withName(final String name) {
        this.name = name;
        return this;
    }

    public BoardDTO withPurpose(final String purpose) {
        this.purpose = purpose;
        return this;
    }


}
