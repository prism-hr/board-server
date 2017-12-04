package hr.prism.board.domain;

import hr.prism.board.representation.TestEmailMessageRepresentation;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "test_email")
public class TestEmail extends BoardEntity {
    
    @Column(name = "email", nullable = false)
    private String email;
    
    @Column(name = "message", nullable = false)
    private TestEmailMessageRepresentation message;
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public TestEmailMessageRepresentation getMessage() {
        return message;
    }
    
    public void setMessage(TestEmailMessageRepresentation message) {
        this.message = message;
    }
    
}
