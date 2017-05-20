package hr.prism.board.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.Objects;

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
    
    @Override
    public int hashCode() {
        return Objects.hash(clientId, code, redirectUri);
    }
    
    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        
        OauthDTO that = (OauthDTO) object;
        return Objects.equals(clientId, that.getClientId()) && Objects.equals(code, that.getCode()) && Objects.equals(redirectUri, that.getRedirectUri());
    }
    
}
