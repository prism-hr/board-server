package hr.prism.board.dto;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.validator.constraints.NotEmpty;

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
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        OAuthDataDTO that = (OAuthDataDTO) other;
        return new EqualsBuilder()
            .append(code, that.code)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(code)
            .toHashCode();
    }

}
