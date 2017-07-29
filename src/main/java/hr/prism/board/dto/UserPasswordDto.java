package hr.prism.board.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class UserPasswordDto {

    @NotNull
    private String uuid;

    @NotNull
    @Size(min = 8, max = 30)
    private String password;

    public String getUuid() {
        return uuid;
    }

    public UserPasswordDto setUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public UserPasswordDto setPassword(String password) {
        this.password = password;
        return this;
    }

}
