package hr.prism.board.authentication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import hr.prism.board.service.AuthenticationService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;

public class AuthenticationFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationFilter.class);

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
            try {
                Claims token = authenticationService.decodeAccessToken(accessToken, jwsSecret);
                long userId = Long.parseLong(token.getSubject());

                SecurityContextHolder.getContext().setAuthentication(new AuthenticationToken(userId));
            } catch (ExpiredJwtException e) {
                LOGGER.warn("JWT token has expired");
            } catch (MalformedJwtException e) {
                LOGGER.error("JWT token is malformed: " + request.getRequestURI(), e);
            }
        }

        filterChain.doFilter(request, response);
    }

}
