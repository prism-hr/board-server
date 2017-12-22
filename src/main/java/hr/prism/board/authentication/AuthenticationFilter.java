package hr.prism.board.authentication;

import hr.prism.board.service.AuthenticationService;
import io.jsonwebtoken.Claims;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class AuthenticationFilter extends OncePerRequestFilter {

    private AuthorizationHeaderResolver authorizationHeaderResolver;

    private AuthenticationService authenticationService;

    private Long sessionRefreshBeforeExpirationSeconds;

    public AuthenticationFilter(AuthenticationService authenticationService,
                                AuthorizationHeaderResolver authorizationHeaderResolver,
                                Long sessionRefreshBeforeExpirationSeconds) {
        this.authenticationService = authenticationService;
        this.authorizationHeaderResolver = authorizationHeaderResolver;
        this.sessionRefreshBeforeExpirationSeconds = sessionRefreshBeforeExpirationSeconds;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization != null) {
            Claims claims = authorizationHeaderResolver.decodeClaims(authorization);
            Long userId = authorizationHeaderResolver.resolveUserId(claims);
            SecurityContextHolder.getContext().setAuthentication(new AuthenticationToken(userId));
            LocalDateTime expiration = LocalDateTime.ofInstant(claims.getExpiration().toInstant(), ZoneId.systemDefault());
            if (expiration.minusSeconds(sessionRefreshBeforeExpirationSeconds).isBefore(LocalDateTime.now())) {
                response.setHeader("Authorization", "Bearer " + authenticationService.makeAccessToken(userId, true));
            }
        }

        filterChain.doFilter(request, response);
    }

}
