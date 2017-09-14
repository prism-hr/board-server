package hr.prism.board.service;

import hr.prism.board.authentication.adapter.OauthAdapter;
import hr.prism.board.domain.User;
import hr.prism.board.dto.*;
import hr.prism.board.enums.DocumentRequestState;
import hr.prism.board.enums.OauthProvider;
import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.BoardForbiddenException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.service.cache.UserCacheService;
import hr.prism.board.service.event.NotificationEventService;
import hr.prism.board.util.BoardUtils;
import hr.prism.board.workflow.Notification;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class AuthenticationService {

    private static Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    private String jwsSecret;

    @Inject
    private UserCacheService userCacheService;

    @Inject
    private UserRoleService userRoleService;

    @Lazy
    @Inject
    private NotificationEventService notificationEventService;

    @Inject
    private ApplicationContext applicationContext;

    @Value("${session.duration.millis}")
    private Long sessionDurationMillis;

    @PostConstruct
    public void postConstruct() {
        BufferedWriter writer = null;
        try {
            String userHome = System.getProperty("user.home");
            File secretFile = new File(userHome + "/jws.secret");
            if (secretFile.exists()) {
                jwsSecret = IOUtils.toString(secretFile.toURI(), StandardCharsets.UTF_8);
            } else {
                writer = new BufferedWriter(new FileWriter(userHome + "/jws.secret"));
                jwsSecret = BoardUtils.randomAlphanumericString(256);
                writer.write(jwsSecret);
            }
        } catch (IOException e) {
            LOGGER.error("Unable to start jws context", e);
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }

    public String getJwsSecret() {
        return jwsSecret;
    }

    public User login(LoginDTO loginDTO) {
        String email = loginDTO.getEmail();
        User user = userCacheService.findByEmailAndPassword(loginDTO.getEmail(), DigestUtils.sha256Hex(loginDTO.getPassword()));
        if (user == null) {
            throw new BoardForbiddenException(ExceptionCode.UNKNOWN_USER, "User: " + email + " cannot be found");
        }

        verifyAuthenticationValid(user, loginDTO);
        return user;
    }

    public User signin(OauthProvider provider, OauthDTO oauthDTO) {
        Class<? extends OauthAdapter> oauthAdapterClass = provider.getOauthAdapter();
        User newUser = applicationContext.getBean(oauthAdapterClass).exchangeForUser(oauthDTO);
        if (newUser.getEmail() == null) {
            throw new BoardForbiddenException(ExceptionCode.UNIDENTIFIABLE_USER, "User not identifiable, no email address");
        }

        String accountId = newUser.getOauthAccountId();
        User user = userCacheService.findByOauthProviderAndOauthAccountId(provider, accountId);
        if (user == null) {
            user = userCacheService.findByEmail(newUser.getEmail());
            if (user == null) {
                user = newUser;
                user.setUuid(UUID.randomUUID().toString());
                user = userCacheService.saveUser(newUser);
            } else {
                user.setOauthProvider(provider);
                user.setOauthAccountId(accountId);
                userCacheService.updateUser(user);
            }
        }

        verifyAuthenticationValid(user, oauthDTO);
        return user;
    }

    public User register(RegisterDTO registerDTO) {
        String uuid = registerDTO.getUuid();
        String email = registerDTO.getEmail();
        if (uuid == null) {
            verifyEmailUnique(email);
            return userCacheService.saveUser(
                new User()
                    .setUuid(UUID.randomUUID().toString())
                    .setGivenName(registerDTO.getGivenName())
                    .setSurname(registerDTO.getSurname())
                    .setEmail(email)
                    .setPassword(DigestUtils.sha256Hex(registerDTO.getPassword()))
                    .setDocumentImageRequestState(DocumentRequestState.DISPLAY_FIRST));
        } else {
            User user = userCacheService.findByUuid(uuid);
            if (user.isRegistered()) {
                throw new BoardForbiddenException(ExceptionCode.DUPLICATE_AUTHENTICATION, "User: " + user.getEmail() + " is already registered");
            }

            if (!email.equals(user.getEmail())) {
                verifyEmailUnique(email);
                user.setEmail(registerDTO.getEmail());
            }

            user.setGivenName(registerDTO.getGivenName());
            user.setSurname(registerDTO.getSurname());
            user.setPassword(DigestUtils.sha256Hex(registerDTO.getPassword()));
            return user;
        }
    }

    public void resetPassword(ResetPasswordDTO resetPasswordDTO) {
        User user = userCacheService.findByEmail(resetPasswordDTO.getEmail());
        if (user == null) {
            throw new BoardForbiddenException(ExceptionCode.UNKNOWN_USER, "User cannot be found");
        }

        String resetUuid = UUID.randomUUID().toString();
        user.setPasswordResetUuid(resetUuid);
        user.setPasswordResetTimestamp(LocalDateTime.now());
        userCacheService.updateUser(user);

        notificationEventService.publishEvent(this,
            Collections.singletonList(new Notification().setUserId(user.getId()).setNotification(hr.prism.board.enums.Notification.RESET_PASSWORD_NOTIFICATION)));
    }

    public String makeAccessToken(Long userId, String jwsSecret) {
        return Jwts.builder()
            .setSubject(userId.toString())
            .setExpiration(new Date(System.currentTimeMillis() + sessionDurationMillis))
            .signWith(SignatureAlgorithm.HS512, jwsSecret)
            .compact();
    }

    public Claims decodeAccessToken(String accessToken, String jwsSecret) {
        return Jwts.parser()
            .setSigningKey(jwsSecret)
            .parseClaimsJws(accessToken)
            .getBody();
    }

    public Map<String, String> refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization == null) {
            throw new BoardException(ExceptionCode.UNAUTHENTICATED_USER, "User not authenticated");
        }

        String accessToken = authorization.replaceFirst("Bearer ", "");
        try {
            Claims token = decodeAccessToken(accessToken, jwsSecret);
            long userId = Long.parseLong(token.getSubject());
            return Collections.singletonMap("token", makeAccessToken(userId, getJwsSecret()));
        } catch (ExpiredJwtException e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Access token expired");
            return null;
        }
    }

    private void verifyEmailUnique(String email) {
        User user = userCacheService.findByEmail(email);
        if (user != null) {
            throw new BoardForbiddenException(ExceptionCode.DUPLICATE_USER, "User: " + email + " exists already");
        }
    }

    private void verifyAuthenticationValid(User user, AuthenticateDTO request) {
        String uuid = request.getUuid();
        if (uuid != null) {
            User invitee = userCacheService.findByUuid(uuid);
            if (invitee.isRegistered() && !user.equals(invitee)) {
                throw new BoardForbiddenException(ExceptionCode.CORRUPTED_AUTHENTICATION, "User: " + user.getEmail() + " cannot accept invitation for: " + invitee.getEmail());
            }

            userRoleService.findByUuid(uuid).setUser(user);
        }
    }

}
