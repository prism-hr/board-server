package hr.prism.board.authentication;

import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.Collections;

public class AuthenticationToken extends AbstractAuthenticationToken {
    
    private Long userId;
    
    public AuthenticationToken(Long userId) {
        super(Collections.emptyList());
        this.userId = userId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    @Override
    public Object getPrincipal() {
        return this.userId;
    }
    
    @Override
    public Object getCredentials() {
        return null;
    }
    
}
