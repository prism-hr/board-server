package hr.prism.board.api;

import hr.prism.board.authentication.PusherAuthenticationDTO;
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
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import java.util.Collections;
import java.util.Map;

@RestController
@SuppressWarnings("SpringAutowiredFieldsWarningInspection")
public class AuthenticationApi {

    @Inject
    private AuthenticationService authenticationService;

    @Inject
    private UserMapper userMapper;

    @RequestMapping(value = "/api/auth/login", method = RequestMethod.POST)
    public Map<String, String> login(@RequestBody @Valid LoginDTO loginDTO, Device device) {
        User user = authenticationService.login(loginDTO);
        return makeAccessTokenResponse(user, device);
    }

    @RequestMapping(value = "/api/auth/register", method = RequestMethod.POST)
    public Map<String, String> register(@RequestBody @Valid RegisterDTO registerDTO, Device device) {
        User user = authenticationService.register(registerDTO);
        return makeAccessTokenResponse(user, device);
    }

    @RequestMapping(value = "/api/auth/{provider}", method = RequestMethod.POST)
    public Map<String, String> signin(@PathVariable String provider, @RequestBody @Valid SigninDTO signinDTO, Device device) {
        User user = authenticationService.signin(OauthProvider.valueOf(provider.toUpperCase()), signinDTO);
        return makeAccessTokenResponse(user, device);
    }

    @RequestMapping(value = "/api/auth/resetPassword", method = RequestMethod.POST)
    public void resetPassword(@RequestBody @Valid ResetPasswordDTO resetPasswordDTO) {
        authenticationService.resetPassword(resetPasswordDTO);
    }

    @RequestMapping(value = "/api/auth/invitee/{invitationUuid}", method = RequestMethod.GET)
    public UserRepresentation getInvitee(@PathVariable String invitationUuid) {
        User invitee = authenticationService.getInvitee(invitationUuid);
        invitee.setRevealEmail(true);
        return userMapper.apply(invitee);
    }

    @RequestMapping(value = "/api/auth/pusher", method = RequestMethod.POST)
    public String authenticatePusher(@RequestBody PusherAuthenticationDTO pusherAuthentication) {
        return authenticationService.authenticatePusher(pusherAuthentication);
    }

    private Map<String, String> makeAccessTokenResponse(User user, Device device) {
        String token = authenticationService.makeAccessToken(
            user.getId(), device == null || device.isNormal());
        return Collections.singletonMap("token", token);
    }

}
