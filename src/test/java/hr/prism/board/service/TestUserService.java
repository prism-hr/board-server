package hr.prism.board.service;

import hr.prism.board.authentication.AuthenticationToken;
import hr.prism.board.domain.User;
import hr.prism.board.dto.RegisterDTO;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.math.BigInteger;
import java.security.SecureRandom;

@Service
public class TestUserService {
    
    @Inject
    private UserService userService;
    
    private SecureRandom random = new SecureRandom();
    
    public synchronized User authenticate() {
        String id = new BigInteger(140, random).toString(30);
        User user = userService.register(new RegisterDTO().setGivenName(id).setSurname(id).setEmail(id + "@example.com").setPassword("password"));
        setAuthentication(user.getId());
        return user;
    }
    
    public void setAuthentication(Long userId) {
        AuthenticationToken authentication = null;
        if (userId != null) {
            authentication = new AuthenticationToken(userId);
        }
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
    
    public void unauthenticate() {
        SecurityContextHolder.getContext().setAuthentication(null);
    }
    
}
