package hr.prism.board.dto;

public class UserDTO {

    private String givenName;

    private String surname;

    private String email;

    public String getGivenName() {
        return givenName;
    }

    public UserDTO setGivenName(String givenName) {
        this.givenName = givenName;
        return this;
    }

    public String getSurname() {
        return surname;
    }

    public UserDTO setSurname(String surname) {
        this.surname = surname;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public UserDTO setEmail(String email) {
        this.email = email;
        return this;
    }
}
