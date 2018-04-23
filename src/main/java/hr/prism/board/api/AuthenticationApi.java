package hr.prism.board.api;

import hr.prism.board.domain.User;
import hr.prism.board.dto.LoginDTO;
import hr.prism.board.dto.RegisterDTO;
import hr.prism.board.dto.ResetPasswordDTO;
import hr.prism.board.dto.SigninDTO;
import hr.prism.board.enums.OauthProvider;
import hr.prism.board.mapper.UserMapper;
import hr.prism.board.representation.UserRepresentation;
import hr.prism.board.service.AuthenticationService;
import org.springframework.mobile.device.Device;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.validation.Valid;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
public class AuthenticationApi {

    private final AuthenticationService authenticationService;

    private final UserMapper userMapper;

    @Inject
    public AuthenticationApi(AuthenticationService authenticationService, UserMapper userMapper) {
        this.authenticationService = authenticationService;
        this.userMapper = userMapper;
    }

    @RequestMapping(value = "/api/auth/login", method = POST)
    public Map<String, String> login(@RequestBody @Valid LoginDTO loginDTO, Device device) {
        User user = authenticationService.login(loginDTO);
        return makeAccessTokenResponse(user, device);
    }

    @RequestMapping(value = "/api/auth/register", method = POST)
    public Map<String, String> register(@RequestBody @Valid RegisterDTO registerDTO, Device device) {
        User user = authenticationService.register(registerDTO);
        return makeAccessTokenResponse(user, device);
    }

    @RequestMapping(value = "/api/auth/{provider}", method = POST)
    public Map<String, String> signin(@PathVariable String provider, @RequestBody @Valid SigninDTO signinDTO,
                                      Device device) {
        User user = authenticationService.signin(OauthProvider.valueOf(provider.toUpperCase()), signinDTO);
        return makeAccessTokenResponse(user, device);
    }

    @RequestMapping(value = "/api/auth/resetPassword", method = POST)
    public void resetPassword(@RequestBody @Valid ResetPasswordDTO resetPasswordDTO) {
        authenticationService.resetPassword(resetPasswordDTO);
    }

    @RequestMapping(value = "/api/auth/invitee/{invitationUuid}", method = GET)
    public UserRepresentation getInvitee(@PathVariable String invitationUuid) {
        User invitee = authenticationService.getInvitee(invitationUuid);
        return userMapper.apply(invitee);
    }

    private Map<String, String> makeAccessTokenResponse(User user, Device device) {
        String token = authenticationService.makeAccessToken(
            user.getId(), device == null || device.isNormal());
        return singletonMap("token", token);
    }

}
