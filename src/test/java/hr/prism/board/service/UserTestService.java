package hr.prism.board.service;

import com.stormpath.sdk.impl.account.DefaultAccount;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserTestService {

    @Inject
    private UserService userService;

    private SecureRandom random = new SecureRandom();

    public synchronized void authenticate() {
        String id = new BigInteger(140, random).toString(30);
        Map<String, Object> properties = new HashMap<>();
        properties.put("email", id);
        properties.put("givenName", id);
        properties.put("surname", id);
        properties.put("href", "https://api.stormpath.com/v1/accounts/" + id);
        userService.createUser(new DefaultAccount(null, properties));

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(new org.springframework.security.core.userdetails.User("https://api" +
            ".stormpath.com/v1/accounts/" + id, "", Collections.emptyList()), null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

}
