package hr.prism.board.dto;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Objects;

public class SigninDTO extends AuthenticateDTO<SigninDTO> {

    @Valid
    @NotNull
    private OAuthAuthorizationDataDTO authorizationData;

    @Valid
    @NotNull
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
    public int hashCode() {
        return Objects.hash(authorizationData, oauthData);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        SigninDTO signinDTO = (SigninDTO) other;
        return Objects.equals(authorizationData, signinDTO.authorizationData)
            && Objects.equals(oauthData, signinDTO.oauthData);
    }

}
