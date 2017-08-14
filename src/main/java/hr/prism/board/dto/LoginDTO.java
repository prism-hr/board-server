package hr.prism.board.dto;

import org.hibernate.validator.constraints.NotEmpty;

public class LoginDTO {

    @NotEmpty
    private String email;

    @NotEmpty
    private String password;

    public String getEmail() {
        return email;
    }

    public LoginDTO setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public LoginDTO setPassword(String password) {
        this.password = password;
        return this;
    }

}
