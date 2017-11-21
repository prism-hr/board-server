package hr.prism.board.authentication;

import hr.prism.board.service.AuthenticationService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class AuthorizationHeaderResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizationHeaderResolver.class);

    @Inject
    private AuthenticationService authenticationService;

    public Long resolveUserId(String authorization) {
        String accessToken = authorization.replaceFirst("Bearer ", "");
        try {
            Claims token = authenticationService.decodeAccessToken(accessToken);
            long userId = Long.parseLong(token.getSubject());

            return userId;
        } catch (ExpiredJwtException e) {
            LOGGER.warn("JWT token has expired");
        } catch (MalformedJwtException e) {
            LOGGER.error("JWT token is malformed", e);
        }
        return null;
    }

}
