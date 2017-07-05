package hr.prism.board.dto;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Size;

public class RegisterDTO {

    @NotEmpty
    @Size(max = 100)
    private String givenName;

    @NotEmpty
    @Size(max = 100)
    private String surname;

    @Email
    @NotEmpty
    @Size(max = 255)
    private String email;

    @Size(min = 8, max = 30)
    private String password;

    public String getGivenName() {
        return givenName;
    }

    public RegisterDTO setGivenName(String givenName) {
        this.givenName = givenName;
        return this;
    }

    public String getSurname() {
        return surname;
    }

    public RegisterDTO setSurname(String surname) {
        this.surname = surname;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public RegisterDTO setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public RegisterDTO setPassword(String password) {
        this.password = password;
        return this;
    }

}
