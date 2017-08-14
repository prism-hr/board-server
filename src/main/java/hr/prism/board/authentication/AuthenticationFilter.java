package hr.prism.board.authentication;

import hr.prism.board.service.AuthenticationService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AuthenticationFilter extends OncePerRequestFilter {

    private String jwsSecret;

    private AuthenticationService authenticationService;

    public AuthenticationFilter(AuthenticationService authenticationService) {
        this.jwsSecret = authenticationService.getJwsSecret();
        this.authenticationService = authenticationService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization != null) {
            String accessToken = authorization.replaceFirst("Bearer ", "");
            Long userId = authenticationService.decodeAccessToken(accessToken, jwsSecret);

            SecurityContextHolder.getContext().setAuthentication(new AuthenticationToken(userId));
            response.setHeader("Authorization", "Bearer " + authenticationService.makeAccessToken(userId, jwsSecret));
        }

        filterChain.doFilter(request, response);
    }

}
