package hr.prism.board.api;

import hr.prism.board.domain.User;
import hr.prism.board.dto.LoginDTO;
import hr.prism.board.dto.OauthDTO;
import hr.prism.board.dto.RegisterDTO;
import hr.prism.board.dto.ResetPasswordDTO;
import hr.prism.board.enums.OauthProvider;
import hr.prism.board.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import java.util.Collections;
import java.util.Map;

@RestController
public class AuthenticationApi {
    
    @Inject
    private UserService userService;
    
    @RequestMapping(value = "/api/auth/login", method = RequestMethod.POST)
    public Map<String, String> login(@RequestBody @Valid LoginDTO loginDTO) {
        User user = userService.login(loginDTO);
        return makeAccessTokenResponse(user);
    }
    
    @RequestMapping(value = "/api/auth/register", method = RequestMethod.POST)
    public Map<String, String> register(@RequestBody @Valid RegisterDTO registerDTO) {
        User user = userService.register(registerDTO);
        return makeAccessTokenResponse(user);
    }
    
    @RequestMapping(value = "/api/auth/{provider}", method = RequestMethod.POST)
    public Map<String, String> signin(@PathVariable String provider, @RequestBody @Valid OauthDTO oauthDTO) {
        User user = userService.signin(OauthProvider.valueOf(provider.toUpperCase()), oauthDTO);
        return makeAccessTokenResponse(user);
    }
    
    @RequestMapping(value = "/api/auth/resetPassword", method = RequestMethod.POST)
    public void resetPassword(@RequestBody @Valid ResetPasswordDTO resetPasswordDTO) {
        userService.resetPassword(resetPasswordDTO);
    }
    
    private Map<String, String> makeAccessTokenResponse(User user) {
        return Collections.singletonMap("token", UserService.makeAccessToken(user.getId()));
    }
    
}
