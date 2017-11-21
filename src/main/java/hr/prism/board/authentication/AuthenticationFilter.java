package hr.prism.board.authentication;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AuthenticationFilter extends OncePerRequestFilter {

    private AuthorizationHeaderResolver authorizationHeaderResolver;

    public AuthenticationFilter(AuthorizationHeaderResolver authorizationHeaderResolver) {
        this.authorizationHeaderResolver = authorizationHeaderResolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization != null) {
            Long userId = authorizationHeaderResolver.resolveUserId(authorization);
            SecurityContextHolder.getContext().setAuthentication(new AuthenticationToken(userId));
        }

        filterChain.doFilter(request, response);
    }

}
