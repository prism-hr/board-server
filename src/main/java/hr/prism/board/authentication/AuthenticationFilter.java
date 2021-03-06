package hr.prism.board.authentication;

import hr.prism.board.domain.User;
import hr.prism.board.service.AuthenticationService;
import hr.prism.board.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.inject.Inject;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static java.lang.Long.parseLong;
import static java.time.LocalDateTime.now;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.security.core.context.SecurityContextHolder.getContext;

@Component
public class AuthenticationFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = getLogger(AuthenticationFilter.class);

    private final Long sessionRefreshBeforeExpirationSeconds;

    private final AuthenticationService authenticationService;

    private final UserService userService;

    @Inject
    public AuthenticationFilter(
        @Value("${session.refreshBeforeExpiration.seconds}") Long sessionRefreshBeforeExpirationSeconds,
        AuthenticationService authenticationService, UserService userService) {
        this.sessionRefreshBeforeExpirationSeconds = sessionRefreshBeforeExpirationSeconds;
        this.authenticationService = authenticationService;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization != null) {
            String accessToken = authorization.replaceFirst("Bearer ", "");
            try {
                Claims claims = authenticationService.decodeAccessToken(accessToken);
                Long userId = parseLong(claims.getSubject());
                User user = userService.getById(userId).setRevealEmail(true);

                getContext().setAuthentication(new AuthenticationToken(user));
                LocalDateTime expiration = LocalDateTime.ofInstant(
                    claims.getExpiration().toInstant(), ZoneId.systemDefault());

                if (expiration.minusSeconds(sessionRefreshBeforeExpirationSeconds).isBefore(now())) {
                    response.setHeader("Authorization",
                        "Bearer " + authenticationService.makeAccessToken(userId, true));
                }
            } catch (ExpiredJwtException e) {
                LOGGER.warn("JWT token has expired");
            } catch (MalformedJwtException e) {
                LOGGER.error("JWT token is malformed", e);
            }
        }

        filterChain.doFilter(request, response);
    }

}
