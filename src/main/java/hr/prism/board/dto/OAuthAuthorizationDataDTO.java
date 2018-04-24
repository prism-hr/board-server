package hr.prism.board.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.Objects;

public class OAuthAuthorizationDataDTO {

    @NotEmpty
    @JsonProperty("client_id")
    private String clientId;

    @NotEmpty
    @JsonProperty("redirect_uri")
    private String redirectUri;

    public String getClientId() {
        return clientId;
    }

    public OAuthAuthorizationDataDTO setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public OAuthAuthorizationDataDTO setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientId, redirectUri);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        OAuthAuthorizationDataDTO that = (OAuthAuthorizationDataDTO) other;
        return Objects.equals(clientId, that.clientId)
            && Objects.equals(redirectUri, that.redirectUri);
    }

}
