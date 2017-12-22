package hr.prism.board.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SigninDTO extends AuthenticateDTO<SigninDTO> {

    @NotNull
    @Valid
    private OAuthAuthorizationDataDTO authorizationData;

    @NotNull
    @Valid
    private OAuthDataDTO oauthData;

    public OAuthAuthorizationDataDTO getAuthorizationData() {
        return authorizationData;
    }

    public SigninDTO setAuthorizationData(OAuthAuthorizationDataDTO authorizationData) {
        this.authorizationData = authorizationData;
        return this;
    }

    public OAuthDataDTO getOauthData() {
        return oauthData;
    }

    public SigninDTO setOauthData(OAuthDataDTO oauthData) {
        this.oauthData = oauthData;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SigninDTO signinDTO = (SigninDTO) o;
        return Objects.equals(authorizationData, signinDTO.authorizationData) &&
            Objects.equals(oauthData, signinDTO.oauthData);
    }

    @Override
    public int hashCode() {

        return Objects.hash(authorizationData, oauthData);
    }
}
