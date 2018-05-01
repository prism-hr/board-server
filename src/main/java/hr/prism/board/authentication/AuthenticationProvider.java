package hr.prism.board.authentication;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import static org.springframework.security.core.context.SecurityContextHolder.getContext;

@Component
public class AuthenticationProvider implements org.springframework.security.authentication.AuthenticationProvider {

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        return getContext().getAuthentication();
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication == AuthenticationToken.class;
    }

}
