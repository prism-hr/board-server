package hr.prism.board.dto;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

public class ResetPasswordDTO {

    @Email
    @NotEmpty
    private String email;

    public String getEmail() {
        return email;
    }

    public ResetPasswordDTO setEmail(String email) {
        this.email = email;
        return this;
    }

}
