package hr.prism.board.service;

import com.stormpath.sdk.impl.account.DefaultAccount;
import hr.prism.board.domain.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserTestService {

    @Inject
    private UserService userService;

    public User createUser(String email) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("email", email);
        properties.put("givenName", email);
        properties.put("surname", email);
        properties.put("href", "https://api.stormpath.com/v1/accounts/" + email);
        return userService.createUser(new DefaultAccount(null, properties));
    }

    public void authenticateAs(String email) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(new org.springframework.security.core.userdetails.User("https://api.stormpath.com/v1/accounts/" + email, "", Collections.emptyList()), null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

}
