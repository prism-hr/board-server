package hr.prism.board.api;

import hr.prism.board.dto.LoginDTO;
import hr.prism.board.dto.OauthDTO;
import hr.prism.board.dto.RegisterDTO;
import hr.prism.board.dto.ResetPasswordDTO;
import hr.prism.board.enums.OauthProvider;
import hr.prism.board.mapper.UserMapper;
import hr.prism.board.representation.UserRepresentation;
import hr.prism.board.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@RestController
public class AuthenticationApi {
    
    @Inject
    private UserService userService;
    
    @Inject
    private UserMapper userMapper;
    
    @RequestMapping(value = "/auth/login", method = RequestMethod.POST)
    public UserRepresentation login(HttpServletResponse response, @RequestBody @Valid LoginDTO loginDTO) {
        return userMapper.apply(userService.login(loginDTO));
    }
    
    @RequestMapping(value = "/auth/register", method = RequestMethod.POST)
    public UserRepresentation register(HttpServletResponse response, @RequestBody @Valid RegisterDTO registerDTO) {
        return userMapper.apply(userService.register(registerDTO));
    }
    
    @RequestMapping(value = "/auth/signin/{provider}", method = RequestMethod.POST)
    public UserRepresentation signin(@PathVariable OauthProvider provider, @RequestBody @Valid OauthDTO oauthDTO) {
        return userMapper.apply(userService.signin(provider, oauthDTO));
    }
    
    @RequestMapping(value = "/auth/resetPassword", method = RequestMethod.POST)
    public void resetPassword(ResetPasswordDTO resetPasswordDTO) {
        userService.resetPassword(resetPasswordDTO);
    }
    
}
