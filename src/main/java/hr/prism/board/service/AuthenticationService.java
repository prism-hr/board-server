package hr.prism.board.service;

import hr.prism.board.authentication.adapter.OauthAdapter;
import hr.prism.board.domain.Resource;
import hr.prism.board.domain.User;
import hr.prism.board.domain.UserRole;
import hr.prism.board.dto.LoginDTO;
import hr.prism.board.dto.RegisterDTO;
import hr.prism.board.dto.ResetPasswordDTO;
import hr.prism.board.dto.SigninDTO;
import hr.prism.board.enums.OauthProvider;
import hr.prism.board.event.ActivityEvent;
import hr.prism.board.event.EventProducer;
import hr.prism.board.event.NotificationEvent;
import hr.prism.board.exception.BoardForbiddenException;
import hr.prism.board.workflow.Notification;
import io.jsonwebtoken.Claims;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;

import static hr.prism.board.enums.Notification.RESET_PASSWORD_NOTIFICATION;
import static hr.prism.board.enums.PasswordHash.SHA256;
import static hr.prism.board.exception.ExceptionCode.*;
import static hr.prism.board.utils.BoardUtils.randomAlphanumericString;
import static io.jsonwebtoken.Jwts.builder;
import static io.jsonwebtoken.Jwts.parser;
import static io.jsonwebtoken.SignatureAlgorithm.HS512;
import static java.lang.System.currentTimeMillis;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.apache.commons.codec.digest.DigestUtils.sha256Hex;

@Service
@Transactional
public class AuthenticationService {

    private final String jwsSecret;

    private final Long sessionDurationSeconds;

    private final UserService userService;

    private final UserRoleService userRoleService;

    private final ActivityService activityService;

    private final EventProducer eventProducer;

    private final EntityManager entityManager;

    private final ApplicationContext applicationContext;

    @Inject
    public AuthenticationService(@Value("${session.duration.seconds}") Long sessionDurationSeconds,
                                 UserService userService, UserRoleService userRoleService,
                                 ActivityService activityService, EventProducer eventProducer,
                                 EntityManager entityManager, ApplicationContext applicationContext)
        throws IOException {
        this.jwsSecret = getJwsSecret();
        this.sessionDurationSeconds = sessionDurationSeconds;
        this.userService = userService;
        this.userRoleService = userRoleService;
        this.activityService = activityService;
        this.eventProducer = eventProducer;
        this.entityManager = entityManager;
        this.applicationContext = applicationContext;
    }

    public User login(LoginDTO loginDTO) throws BoardForbiddenException {
        String email = loginDTO.getEmail();
        User user = userService.getByEmail(loginDTO.getEmail());
        if (user == null) {
            throw new BoardForbiddenException(UNKNOWN_USER, "User: " + email + " cannot be found");
        }

        if (!user.passwordMatches(loginDTO.getPassword())) {
            throw new BoardForbiddenException(UNAUTHENTICATED_USER, "Incorrect password for user: " + email);
        }

        String uuid = loginDTO.getUuid();
        if (uuid != null) {
            processInvitation(user, uuid);
        }

        return user;
    }

    public User signin(OauthProvider provider, SigninDTO signinDTO) {
        Class<? extends OauthAdapter> oauthAdapterClass = provider.getOauthAdapter();
        User oauth = applicationContext.getBean(oauthAdapterClass).exchangeForUser(signinDTO);
        if (oauth.getEmail() == null) {
            throw new BoardForbiddenException(UNIDENTIFIABLE_USER, "User not identifiable, no email address");
        }

        String accountId = oauth.getOauthAccountId();
        User user = userService.getByOauthCredentials(provider, accountId);
        if (user == null) {
            user = userService.getByEmail(oauth.getEmail());
            if (user == null) {
                User invitee = null;
                String uuid = signinDTO.getUuid();
                if (uuid != null) {
                    invitee = userService.getByUserRoleUuid(signinDTO.getUuid());
                }

                if (invitee == null || invitee.isRegistered()) {
                    user = oauth;
                    user.setUuid(randomUUID().toString());
                    user = userService.saveUser(user);
                } else {
                    user = invitee;
                    user.setGivenName(oauth.getGivenName());
                    user.setSurname(oauth.getSurname());
                    user.setEmail(oauth.getEmail());
                    user.setOauthProvider(oauth.getOauthProvider());
                    user.setOauthAccountId(oauth.getOauthAccountId());
                    user.setDocumentImageRequestState(oauth.getDocumentImageRequestState());
                    user = userService.updateUserIndex(user);
                }
            } else {
                user.setOauthProvider(provider);
                user.setOauthAccountId(accountId);
                userService.updateUser(user);
            }
        }

        String uuid = signinDTO.getUuid();
        if (uuid != null) {
            processInvitation(user, uuid);
        }

        return user;
    }

    public User register(RegisterDTO registerDTO) {
        String uuid = registerDTO.getUuid();
        String email = registerDTO.getEmail();
        if (uuid == null) {
            checkUniqueEmail(email);
            return userService.createUser(registerDTO);
        } else {
            User invitee = userService.getByUserRoleUuid(uuid);
            if (invitee.isRegistered()) {
                throw new BoardForbiddenException(DUPLICATE_REGISTRATION,
                    "User: " + invitee.getEmail() + " is already registered");
            }

            if (!email.equals(invitee.getEmail())) {
                checkUniqueEmail(email);
            }

            invitee.setEmail(registerDTO.getEmail());
            invitee.setGivenName(registerDTO.getGivenName());
            invitee.setSurname(registerDTO.getSurname());
            invitee.setPassword(sha256Hex(registerDTO.getPassword()));
            invitee.setPasswordHash(SHA256);
            return invitee;
        }
    }

    public void resetPassword(ResetPasswordDTO resetPasswordDTO) {
        User user = userService.getByEmail(resetPasswordDTO.getEmail());
        if (user == null) {
            throw new BoardForbiddenException(UNKNOWN_USER, "User cannot be found");
        }

        String resetUuid = randomUUID().toString();
        user.setPasswordResetUuid(resetUuid);
        user.setPasswordResetTimestamp(LocalDateTime.now());
        userService.updateUser(user);

        eventProducer.produce(
            new NotificationEvent(this,
                singletonList(
                    new Notification()
                        .setUserId(user.getId())
                        .setNotification(RESET_PASSWORD_NOTIFICATION))));
    }

    public User getInvitee(String uuid) {
        return userService.getByUserRoleUuid(uuid)
            .setRevealEmail(true);
    }

    public String makeAccessToken(Long userId, boolean specifyExpirationDate) {
        return builder()
            .setSubject(userId.toString())
            .setExpiration(
                specifyExpirationDate ? new Date(currentTimeMillis() + sessionDurationSeconds * 1000) : null)
            .signWith(HS512, jwsSecret)
            .compact();
    }

    public Claims decodeAccessToken(String accessToken) {
        return parser()
            .setSigningKey(jwsSecret)
            .parseClaimsJws(accessToken)
            .getBody();
    }

    private void checkUniqueEmail(String email) {
        User user = userService.getByEmail(email);
        if (user != null) {
            throw new BoardForbiddenException(DUPLICATE_USER, "User: " + email + " exists already");
        }
    }

    private void processInvitation(User user, String uuid) {
        User invitee = userService.getByUserRoleUuid(uuid);
        if (!user.equals(invitee)) {
            UserRole userRole = userRoleService.getByUuid(uuid);
            Resource resource = userRole.getResource();

            userRoleService.deleteUserRole(userRole.getResource(), user, userRole.getRole());
            entityManager.flush();
            userRole.setUser(user);

            if (!invitee.isRegistered()) {
                userRoleService.mergeUserRoles(user, invitee);
                activityService.deleteActivityUsers(invitee);
                userService.deleteUser(invitee);
            }

            eventProducer.produce(
                new ActivityEvent(this, resource.getId()));
        }
    }

    private String getJwsSecret() throws IOException {
        String userHome = System.getProperty("user.home");
        File secretFile = new File(userHome + "/jws.secret");
        if (secretFile.exists()) {
            return IOUtils.toString(secretFile.toURI(), UTF_8);
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(userHome + "/jws.secret"))) {
            String jwsSecret = randomAlphanumericString(256);
            writer.write(jwsSecret);
            return jwsSecret;
        }
    }

}
