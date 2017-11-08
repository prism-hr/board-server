package hr.prism.board.api;

import hr.prism.board.domain.User;
import hr.prism.board.dto.LoginDTO;
import hr.prism.board.dto.RegisterDTO;
import hr.prism.board.dto.ResetPasswordDTO;
import hr.prism.board.dto.SigninDTO;
import hr.prism.board.enums.OauthProvider;
import hr.prism.board.service.AuthenticationService;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@RestController
@SuppressWarnings("SpringAutowiredFieldsWarningInspection")
public class AuthenticationApi {

    @Inject
    private AuthenticationService authenticationService;

    @RequestMapping(value = "/api/auth/login", method = RequestMethod.POST)
    public Map<String, String> login(@RequestBody @Valid LoginDTO loginDTO) {
        User user = authenticationService.login(loginDTO);
        return makeAccessTokenResponse(user);
    }

    @RequestMapping(value = "/api/auth/register", method = RequestMethod.POST)
    public Map<String, String> register(@RequestBody @Valid RegisterDTO registerDTO) {
        User user = authenticationService.register(registerDTO);
        return makeAccessTokenResponse(user);
    }

    @RequestMapping(value = "/api/auth/{provider}", method = RequestMethod.POST)
    public Map<String, String> signin(@PathVariable String provider, @RequestBody @Valid SigninDTO signinDTO) {
        User user = authenticationService.signin(OauthProvider.valueOf(provider.toUpperCase()), signinDTO);
        return makeAccessTokenResponse(user);
    }

    @RequestMapping(value = "/api/auth/resetPassword", method = RequestMethod.POST)
    public void resetPassword(@RequestBody @Valid ResetPasswordDTO resetPasswordDTO) {
        authenticationService.resetPassword(resetPasswordDTO);
    }

    @RequestMapping(value = "/api/auth/refreshToken", method = RequestMethod.GET)
    public Map<String, String> refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        return authenticationService.refreshToken(request, response);
    }

    private Map<String, String> makeAccessTokenResponse(User user) {
        return Collections.singletonMap("token", authenticationService.makeAccessToken(user.getId(), authenticationService.getJwsSecret()));
    }

}
