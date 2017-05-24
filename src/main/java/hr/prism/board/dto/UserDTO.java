package hr.prism.board.dto;

import org.hibernate.validator.constraints.Email;

import javax.validation.constraints.Size;

public class UserDTO {

    @Size(min = 1, max = 100)
    private String givenName;

    @Size(min = 1, max = 100)
    private String surname;

    @Email
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
