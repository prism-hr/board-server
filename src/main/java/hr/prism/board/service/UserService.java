package hr.prism.board.service;

import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.servlet.account.AccountResolver;
import hr.prism.board.dao.EntityDAO;
import hr.prism.board.domain.User;
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
    private EntityDAO entityDAO;

    public User getCurrentUser() {
        org.springframework.security.core.userdetails.User user = (org.springframework.security.core.userdetails.User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String href = user.getUsername();
        String stormpathId = href.substring(href.lastIndexOf('/'));
        return entityDAO.getByProperty(User.class, "stormpathId", stormpathId);
    }

    public void createUser(Account account) {
        User user = new User();
        user.setEmail(account.getEmail());
        user.setGivenName(account.getGivenName());
        user.setSurname(account.getSurname());
        String href = account.getHref();
        String stormpathId = href.substring(href.lastIndexOf('/'));
        user.setStormpathId(stormpathId);
        entityDAO.save(user);
    }
}
