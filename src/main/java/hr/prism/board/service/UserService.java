package hr.prism.board.service;

import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.servlet.account.AccountResolver;
import hr.prism.board.domain.User;
import hr.prism.board.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

@Service
@Transactional
public class UserService {
    
    @Inject
    private AccountResolver accountResolver;
    
    @Inject
    private UserRepository userRepository;
    
    public User getCurrentUser() {
        org.springframework.security.core.userdetails.User user = (org.springframework.security.core.userdetails.User) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        String href = user.getUsername();
        String stormpathId = href.substring(href.lastIndexOf('/') + 1);
        return userRepository.findByStormpathId(stormpathId);
    }
    
    public User createUser(Account account) {
        User user = new User();
        user.setEmail(account.getEmail());
        user.setGivenName(account.getGivenName());
        user.setSurname(account.getSurname());
        String href = account.getHref();
        String stormpathId = href.substring(href.lastIndexOf('/') + 1);
        user.setStormpathId(stormpathId);
        return userRepository.save(user);
    }
    
    public User getByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
}
