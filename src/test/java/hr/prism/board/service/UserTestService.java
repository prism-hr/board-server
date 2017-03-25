package hr.prism.board.service;

import com.stormpath.sdk.impl.account.DefaultAccount;
import hr.prism.board.domain.User;
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
    
    private static String STORMPATH_API_PATH = "https://api.stormpath.com/v1/accounts/";
    
    @Inject
    private UserService userService;
    
    private SecureRandom random = new SecureRandom();
    
    public synchronized User authenticate() {
        String id = new BigInteger(140, random).toString(30);
        Map<String, Object> properties = new HashMap<>();
        properties.put("email", id + "@example.com");
        properties.put("givenName", id);
        properties.put("surname", id);
        properties.put("href", STORMPATH_API_PATH + id);
        User user = userService.createUser(new DefaultAccount(null, properties));
        
        setAuthentication(id);
        return user;
    }
    
    public void setAuthentication(String id) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
            new org.springframework.security.core.userdetails.User(STORMPATH_API_PATH + id, "", Collections.emptyList()), null));
    }
    
}
