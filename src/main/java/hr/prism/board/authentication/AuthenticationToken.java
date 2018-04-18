package hr.prism.board.authentication;

import hr.prism.board.domain.User;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import static java.util.Collections.emptyList;

public class AuthenticationToken extends AbstractAuthenticationToken {

    private User user;

    public AuthenticationToken(User user) {
        super(emptyList());
        this.user = user;
    }

    public User getUser() {
        return this.user;
    }

    @Override
    public Object getPrincipal() {
        return this.user;
    }

    @Override
    public Object getCredentials() {
        return null;
    }

}
