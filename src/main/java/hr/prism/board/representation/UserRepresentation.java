package hr.prism.board.representation;

public class UserRepresentation {
    
    private Long id;
    
    private String givenName;
    
    private String surname;
    
    private String email;
    
    public Long getId() {
        return id;
    }
    
    public UserRepresentation setId(Long id) {
        this.id = id;
        return this;
    }
    
    public String getGivenName() {
        return givenName;
    }
    
    public UserRepresentation setGivenName(String givenName) {
        this.givenName = givenName;
        return this;
    }
    
    public String getSurname() {
        return surname;
    }
    
    public UserRepresentation setSurname(String surname) {
        this.surname = surname;
        return this;
    }
    
    public String getEmail() {
        return email;
    }
    
    public UserRepresentation setEmail(String email) {
        this.email = email;
        return this;
    }
    
}
