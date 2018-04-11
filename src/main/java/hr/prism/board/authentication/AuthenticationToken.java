package hr.prism.board.authentication;

import org.springframework.security.authentication.AbstractAuthenticationToken;

import static java.util.Collections.emptyList;

public class AuthenticationToken extends AbstractAuthenticationToken {

    private Long userId;

    public AuthenticationToken(Long userId) {
        super(emptyList());
        this.userId = userId;
    }

    public Long getUserId() {
        return this.userId;
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
