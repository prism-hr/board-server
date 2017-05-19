package hr.prism.board.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.validator.constraints.NotEmpty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OauthDTO {

    @NotEmpty
    private String clientId;

    @NotEmpty
    private String code;

    @NotEmpty
    private String redirectUri;

    public String getClientId() {
        return clientId;
    }

    public OauthDTO setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    public String getCode() {
        return code;
    }

    public OauthDTO setCode(String code) {
        this.code = code;
        return this;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public OauthDTO setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
        return this;
    }

}
