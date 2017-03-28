package hr.prism.board.service;

import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.servlet.account.AccountResolver;
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
    private AccountResolver accountResolver;

    @Inject
    private UserRepository userRepository;

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }

        org.springframework.security.core.userdetails.User user = (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
        if (user == null) {
            return null;
        }

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

    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
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

}
