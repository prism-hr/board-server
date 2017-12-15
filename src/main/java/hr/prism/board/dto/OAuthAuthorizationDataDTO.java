package hr.prism.board.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OAuthAuthorizationDataDTO {

    @JsonProperty("client_id")
    @NotEmpty
    private String clientId;

    @JsonProperty("redirect_uri")
    @NotEmpty
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OAuthAuthorizationDataDTO that = (OAuthAuthorizationDataDTO) o;
        return Objects.equals(clientId, that.clientId) &&
            Objects.equals(redirectUri, that.redirectUri);
    }

    @Override
    public int hashCode() {

        return Objects.hash(clientId, redirectUri);
    }
}
