package hr.prism.board.api;

import hr.prism.board.domain.User;
import hr.prism.board.dto.UserPasswordDTO;
import hr.prism.board.dto.UserPatchDTO;
import hr.prism.board.mapper.UserMapper;
import hr.prism.board.representation.UserRepresentation;
import hr.prism.board.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.validation.Valid;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.PATCH;

@RestController
public class UserApi {

    private final UserService userService;

    private final UserMapper userMapper;

    @Inject
    public UserApi(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/api/user", method = GET)
    public UserRepresentation getUser(@AuthenticationPrincipal User user) {
        return userMapper.apply(user);
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/api/user", method = PATCH)
    public UserRepresentation updateUser(@AuthenticationPrincipal User user, @RequestBody @Valid UserPatchDTO userDTO) {
        User updatedUser = userService.updateUser(user, userDTO);
        return userMapper.apply(updatedUser);
    }

    @RequestMapping(value = "/api/user/password", method = PATCH)
    public void resetPassword(@RequestBody @Valid UserPasswordDTO userPasswordDTO) {
        userService.resetPassword(userPasswordDTO);
    }

}
