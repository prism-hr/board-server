package hr.prism.board.dto;

import hr.prism.board.enums.OauthProvider;
import org.hibernate.validator.constraints.NotEmpty;

public class OauthDTO {
    
    @NotEmpty
    private OauthProvider provider;
    
    @NotEmpty
    private String clientId;
    
    @NotEmpty
    private String code;
    
    private String secret;
    
    @NotEmpty
    private String redirectUri;
    
    public OauthProvider getProvider() {
        return provider;
    }
    
    public OauthDTO setProvider(OauthProvider provider) {
        this.provider = provider;
        return this;
    }
    
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
    
    public String getSecret() {
        return secret;
    }
    
    public OauthDTO setSecret(String secret) {
        this.secret = secret;
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
