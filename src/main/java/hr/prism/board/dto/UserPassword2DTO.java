package hr.prism.board.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class UserPassword2DTO {

    @NotNull
    private String uuid;

    @NotNull
    @Size(min = 8, max = 30)
    private String password;

    public String getUuid() {
        return uuid;
    }

    public UserPassword2DTO setUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public UserPassword2DTO setPassword(String password) {
        this.password = password;
        return this;
    }

}
