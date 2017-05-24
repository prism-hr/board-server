package hr.prism.board.service;

import com.google.common.collect.ImmutableMap;
import hr.prism.board.authentication.AuthenticationToken;
import hr.prism.board.authentication.adapter.OauthAdapter;
import hr.prism.board.domain.Document;
import hr.prism.board.domain.User;
import hr.prism.board.dto.*;
import hr.prism.board.enums.DocumentRequestState;
import hr.prism.board.enums.OauthProvider;
import hr.prism.board.exception.ApiForbiddenException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.repository.UserRepository;
import hr.prism.board.service.cache.UserCacheService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

@Service
@Transactional
public class UserService {
    
    @Inject
    private UserRepository userRepository;
    
    @Inject
    private UserCacheService userCacheService;
    
    @Inject
    private DocumentService documentService;
    
    @Inject
    private NotificationService notificationService;
    
    @Inject
    private ApplicationContext applicationContext;
    
    @Inject
    private Environment environment;
    
    public User findOne(Long id) {
        return userRepository.findOne(id);
    }
    
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        
        Long userId = ((AuthenticationToken) authentication).getUserId();
        if (userId == null) {
            return null;
        }
    
        return userCacheService.findOne(userId);
    }
    
    public User getCurrentUserSecured() {
        User user = getCurrentUser();
        if (user == null) {
            throw new ApiForbiddenException(ExceptionCode.UNAUTHENTICATED_USER);
        }
        
        return user;
    }
    
    public User login(LoginDTO loginDTO) {
        User user = userRepository.findByEmailAndPassword(loginDTO.getEmail(), DigestUtils.sha256Hex(loginDTO.getPassword()), LocalDateTime.now());
        if (user == null) {
            throw new ApiForbiddenException(ExceptionCode.UNREGISTERED_USER);
        }
        
        return user;
    }
    
    public User register(RegisterDTO registerDTO) {
        String email = registerDTO.getEmail();
        User user = userRepository.findByEmail(email);
        if (user != null) {
            throw new ApiForbiddenException(ExceptionCode.DUPLICATE_USER);
        }
    
        return userRepository.save(new User().setGivenName(registerDTO.getGivenName()).setSurname(registerDTO.getSurname()).setEmail(email)
            .setPassword(DigestUtils.sha256Hex(registerDTO.getPassword())).setDocumentImageRequestState(DocumentRequestState.DISPLAY_FIRST));
    }
    
    public User signin(OauthProvider provider, OauthDTO oauthDTO) {
        Class<? extends OauthAdapter> oauthAdapterClass = provider.getOauthAdapter();
        if (oauthAdapterClass == null) {
            throw new ApiForbiddenException(ExceptionCode.UNSUPPORTED_AUTHENTICATOR);
        }
        
        User newUser = applicationContext.getBean(oauthAdapterClass).exchangeForUser(oauthDTO);
        if (newUser.getEmail() == null) {
            throw new ApiForbiddenException(ExceptionCode.UNIDENTIFIABLE_USER);
        }
    
        String accountId = newUser.getOauthAccountId();
        User oldUser = userRepository.findByOauthProviderAndOauthAccountId(provider, accountId);
        if (oldUser == null) {
            String email = newUser.getEmail();
            oldUser = userRepository.findByEmail(email);
            if (oldUser == null) {
                return userRepository.save(newUser);
            } else {
                oldUser.setOauthProvider(provider);
                oldUser.setOauthAccountId(accountId);
            }
        }
        
        return oldUser;
    }
    
    public void resetPassword(ResetPasswordDTO resetPasswordDTO) {
        User user = userRepository.findByEmail(resetPasswordDTO.getEmail());
        if (user == null) {
            throw new ApiForbiddenException(ExceptionCode.UNREGISTERED_USER);
        }
    
        String temporaryPassword = RandomStringUtils.randomAlphabetic(12);
        user.setTemporaryPassword(DigestUtils.sha256Hex(temporaryPassword));
        user.setTemporaryPasswordExpiryTimestamp(LocalDateTime.now().plusHours(1));
        userRepository.update(user);
        
        String serverUrl = environment.getProperty("server.url");
        String redirectUrl = RedirectService.makeRedirectForLogin(serverUrl);
        NotificationService.Notification notification = notificationService.makeNotification(
            "reset_password", user, ImmutableMap.of("temporaryPassword", temporaryPassword, "redirectUrl", redirectUrl));
        notificationService.sendNotification(notification);
    }
    
    public User updateUser(UserPatchDTO userDTO) {
        User user = getCurrentUserSecured();
        Optional<String> givenNameOptional = userDTO.getGivenName();
        if (givenNameOptional != null) {
            user.setGivenName(givenNameOptional.orElse(user.getGivenName()));
        }
    
        Optional<String> surnameOptional = userDTO.getSurname();
        if (surnameOptional != null) {
            user.setSurname(surnameOptional.orElse(user.getSurname()));
        }
    
        Optional<DocumentDTO> documentImageOptional = userDTO.getDocumentImage();
        if (documentImageOptional != null) {
            Document oldImage = user.getDocumentImage();
            DocumentDTO newImage = documentImageOptional.orElse(null);
            if (newImage != null) {
                user.setDocumentImage(documentService.getOrCreateDocument(newImage));
            }
            
            if (oldImage != null && (newImage == null || !oldImage.getId().equals(newImage.getId()))) {
                documentService.deleteDocument(oldImage);
            }
        }
    
        Optional<DocumentRequestState> documentRequestStateOptional = userDTO.getDocumentImageRequestState();
        if (documentRequestStateOptional != null) {
            user.setDocumentImageRequestState(documentRequestStateOptional.orElse(user.getDocumentImageRequestState()));
        }
        
        userRepository.update(user);
        return user;
    }
    
    public User getOrCreateUser(UserDTO userDTO) {
        User user = userRepository.findByEmail(userDTO.getEmail());
        if (user == null) {
            user = new User();
            user.setEmail(userDTO.getEmail());
            user.setGivenName(userDTO.getGivenName());
            user.setSurname(userDTO.getSurname());
            return userRepository.save(user);
        }
    
        return user;
    }
    
    public static String makeAccessToken(Long userId) {
        // FIXME: externalise the secret
        return Jwts.builder()
            .setSubject(userId.toString())
            .setExpiration(new Date(System.currentTimeMillis() + 3600000))
            .signWith(SignatureAlgorithm.HS512, "secret")
            .compact();
    }
    
    public static Long decodeAccessToken(String accessToken) {
        // FIXME: externalise the secret
        return Long.parseLong(Jwts.parser().setSigningKey("secret").parseClaimsJws(accessToken).getBody().getSubject());
    }
    
}
