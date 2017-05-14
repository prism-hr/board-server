package hr.prism.board.dto;

import hr.prism.board.enums.OAuthProvider;
import org.hibernate.validator.constraints.NotEmpty;

public class OauthDTO {
    
    @NotEmpty
    private OAuthProvider provider;
    
    @NotEmpty
    private String clientId;
    
    @NotEmpty
    private String code;
    
    @NotEmpty
    private String redirectUri;
    
    public OAuthProvider getProvider() {
        return provider;
    }
    
    public OauthDTO setProvider(OAuthProvider provider) {
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
    
    public String getRedirectUri() {
        return redirectUri;
    }
    
    public OauthDTO setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
        return this;
    }
    
}
