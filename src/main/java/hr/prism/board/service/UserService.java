package hr.prism.board.service;

import com.stormpath.sdk.account.Account;
import hr.prism.board.domain.User;
import hr.prism.board.exception.ApiForbiddenException;
import hr.prism.board.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

@Service
@Transactional
public class UserService {
    
    @Inject
    private UserRepository userRepository;
    
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        
        Object principal = authentication.getPrincipal();
        if (principal == null || !(principal instanceof org.springframework.security.core.userdetails.User)) {
            return null;
        }
        
        org.springframework.security.core.userdetails.User user = (org.springframework.security.core.userdetails.User) principal;
        String username = user.getUsername();
        String stormpathId = username.substring(username.lastIndexOf('/') + 1);
        return userRepository.findByStormpathId(stormpathId);
    }
    
    public User getCurrentUserSecured() {
        User user = getCurrentUser();
        if (user == null) {
            throw new ApiForbiddenException("User not authenticated");
        }
        
        return user;
    }
    
    public User getBoardBotUser() {
        return userRepository.findByStormpathIdNull();
    }
    
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    public User createUser(Account account) {
        User user = new User();
        user.setEmail(account.getEmail());
        user.setGivenName(account.getGivenName());
        user.setSurname(account.getSurname());
        
        // Board bot is identified by having no stormpathId
        // Should be safe in all other cases as stormpath will validate href not null
        String href = account.getHref();
        if (href != null) {
            String stormpathId = href.substring(href.lastIndexOf('/') + 1);
            user.setStormpathId(stormpathId);
        }
        
        return userRepository.save(user);
    }
    
}
