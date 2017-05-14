package hr.prism.board.service;

import hr.prism.board.authentication.AuthenticationToken;
import hr.prism.board.domain.Document;
import hr.prism.board.domain.User;
import hr.prism.board.dto.*;
import hr.prism.board.exception.ApiForbiddenException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.Date;

@Service
@Transactional
public class UserService {
    
    @Inject
    private UserRepository userRepository;
    
    @Inject
    private DocumentService documentService;
    
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
        
        return userRepository.findOne(userId);
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
        
        return userRepository.save(new User().setGivenName(registerDTO.getGivenName())
            .setSurname(registerDTO.getSurname()).setEmail(email).setPassword(DigestUtils.sha256Hex(registerDTO.getPassword())));
    }
    
    public User signin(OauthDTO oauthDTO) {
        return null;
    }
    
    public void resetPassword(ResetPasswordDTO resetPasswordDTO) {
        User user = userRepository.findByEmail(resetPasswordDTO.getEmail());
        if (user == null) {
            throw new ApiForbiddenException(ExceptionCode.UNREGISTERED_USER);
        }
        
        // TODO: send email with temporary password
        String temporaryPassword = RandomStringUtils.randomAlphabetic(12);
        
        user.setTemporaryPassword(DigestUtils.sha256Hex(temporaryPassword));
        user.setTemporaryPasswordExpiryTimestamp(LocalDateTime.now().plusHours(1));
    }
    
    public User updateUser(UserPatchDTO userDTO) {
        User user = getCurrentUser();
        if (userDTO.getGivenName() != null) {
            user.setGivenName(userDTO.getGivenName().orElse(null));
        }
        if (userDTO.getSurname() != null) {
            user.setSurname(userDTO.getSurname().orElse(null));
        }
        if (userDTO.getDocumentImage() != null) {
            Document oldImage = user.getDocumentImage();
            DocumentDTO newImage = userDTO.getDocumentImage().orElse(null);
            if (oldImage != null && !oldImage.getId().equals(newImage.getId())) {
                documentService.deleteDocument(oldImage);
            }
            user.setDocumentImage(documentService.getOrCreateDocument(newImage));
        }
        
        userRepository.update(user);
        return user;
    }
    
    public String makeAccessToken(Long userId) {
        // FIXME: externalise the secret
        // We use the user ID as the subject as the email address might change, we don't want to have to change the token
        return Jwts.builder()
            .setSubject(userId.toString())
            .setExpiration(new Date(System.currentTimeMillis() + 3600000))
            .signWith(SignatureAlgorithm.HS512, "secret")
            .compact();
    }
    
    public Long decodeAccessToken(String accessToken) {
        // FIXME: externalise the secret
        // We don't need to check whether the user ID is valid here, as that would be validated later in the request
        Jws<Claims> claims = Jwts.parser().setSigningKey("secret").parseClaimsJws(accessToken);
        return Long.parseLong(claims.getBody().getSubject());
    }
    
}
