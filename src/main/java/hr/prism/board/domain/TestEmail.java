package hr.prism.board.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import hr.prism.board.representation.TestEmailMessageRepresentation;

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
