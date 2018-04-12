package hr.prism.board.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OAuthDataDTO {

    @NotEmpty
    private String code;

    public String getCode() {
        return code;
    }

    public OAuthDataDTO setCode(String code) {
        this.code = code;
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        return Objects.equals(code, ((OAuthDataDTO) other).code);
    }

}
