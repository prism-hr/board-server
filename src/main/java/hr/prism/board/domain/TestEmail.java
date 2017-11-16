package hr.prism.board.domain;

import hr.prism.board.representation.TestEmailMessageRepresentation;

import javax.persistence.*;

@Entity
@Table(name = "test_email")
public class TestEmail extends BoardEntity {

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "message", nullable = false)
    private TestEmailMessageRepresentation message;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public TestEmailMessageRepresentation getMessage() {
        return message;
    }

    public void setMessage(TestEmailMessageRepresentation message) {
        this.message = message;
    }
}
