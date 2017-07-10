package hr.prism.board.service;

import com.google.common.collect.ImmutableMap;
import hr.prism.board.authentication.adapter.OauthAdapter;
import hr.prism.board.domain.User;
import hr.prism.board.dto.LoginDTO;
import hr.prism.board.dto.OauthDTO;
import hr.prism.board.dto.RegisterDTO;
import hr.prism.board.dto.ResetPasswordDTO;
import hr.prism.board.enums.DocumentRequestState;
import hr.prism.board.enums.OauthProvider;
import hr.prism.board.exception.BoardForbiddenException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.repository.UserRepository;
import hr.prism.board.service.cache.UserCacheService;
import hr.prism.board.service.event.NotificationEventService;
import hr.prism.board.util.BoardUtils;
import hr.prism.board.workflow.Notification;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

@Service
@Transactional
public class AuthenticationService {

    private static Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    private String jwsSecret;

    private Long jwsSessionDuration;

    @Inject
    private UserRepository userRepository;

    @Inject
    private UserCacheService userCacheService;

    @Lazy
    @Inject
    private NotificationEventService notificationEventService;

    @Inject
    private ApplicationContext applicationContext;

    @Inject
    private Environment environment;

    @PostConstruct
    public void postConstruct() {
        BufferedWriter writer = null;
        jwsSessionDuration = Long.parseLong(environment.getProperty("session.duration.millis"));
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
        User user = userRepository.findByEmailAndPassword(loginDTO.getEmail(), DigestUtils.sha256Hex(loginDTO.getPassword()), LocalDateTime
            .now());
        if (user == null) {
            throw new BoardForbiddenException(ExceptionCode.UNREGISTERED_USER);
        }

        return user;
    }

    public User register(RegisterDTO registerDTO) {
        String email = registerDTO.getEmail();
        User user = userRepository.findByEmail(email);
        if (user != null) {
            throw new BoardForbiddenException(ExceptionCode.DUPLICATE_USER);
        }

        return userCacheService.saveUser(new User().setGivenName(registerDTO.getGivenName())
            .setSurname(registerDTO.getSurname())
            .setEmail(email)
            .setPassword(DigestUtils.sha256Hex(registerDTO.getPassword()))
            .setDocumentImageRequestState(DocumentRequestState.DISPLAY_FIRST));
    }

    public User signin(OauthProvider provider, OauthDTO oauthDTO) {
        Class<? extends OauthAdapter> oauthAdapterClass = provider.getOauthAdapter();
        if (oauthAdapterClass == null) {
            throw new BoardForbiddenException(ExceptionCode.UNSUPPORTED_AUTHENTICATOR);
        }

        User newUser = applicationContext.getBean(oauthAdapterClass).exchangeForUser(oauthDTO);
        if (newUser.getEmail() == null) {
            throw new BoardForbiddenException(ExceptionCode.UNIDENTIFIABLE_USER);
        }

        String accountId = newUser.getOauthAccountId();
        User oldUser = userRepository.findByOauthProviderAndOauthAccountId(provider, accountId);
        if (oldUser == null) {
            String email = newUser.getEmail();
            oldUser = userRepository.findByEmail(email);
            if (oldUser == null) {
                newUser.setUuid(UUID.randomUUID().toString());
                return userCacheService.saveUser(newUser);
            } else {
                oldUser.setOauthProvider(provider);
                oldUser.setOauthAccountId(accountId);
                userCacheService.updateUser(oldUser);
            }
        }

        return oldUser;
    }

    public void resetPassword(ResetPasswordDTO resetPasswordDTO) {
        User user = userRepository.findByEmail(resetPasswordDTO.getEmail());
        if (user == null) {
            throw new BoardForbiddenException(ExceptionCode.UNREGISTERED_USER);
        }

        String temporaryPassword = BoardUtils.randomAlphanumericString(12);
        user.setTemporaryPassword(DigestUtils.sha256Hex(temporaryPassword));
        user.setTemporaryPasswordExpiryTimestamp(LocalDateTime.now().plusHours(1));
        userCacheService.updateUser(user);

        notificationEventService.publishEvent(this, Collections.singletonList(new Notification().setUserId(user.getId())
            .setCustomProperties(ImmutableMap.of("temporaryPassword", temporaryPassword)).setNotification(hr.prism.board.enums.Notification.RESET_PASSWORD)));
    }

    public String makeAccessToken(Long userId, String jwsSecret) {
        return Jwts.builder()
            .setSubject(userId.toString())
            .setExpiration(new Date(System.currentTimeMillis() + jwsSessionDuration))
            .signWith(SignatureAlgorithm.HS512, jwsSecret)
            .compact();
    }

    public Long decodeAccessToken(String accessToken, String jwsSecret) {
        return Long.parseLong(Jwts.parser()
            .setSigningKey(jwsSecret)
            .parseClaimsJws(accessToken)
            .getBody()
            .getSubject());
    }

}
