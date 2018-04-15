package hr.prism.board.api;

import hr.prism.board.domain.User;
import hr.prism.board.dto.UserPasswordDTO;
import hr.prism.board.dto.UserPatchDTO;
import hr.prism.board.mapper.UserMapper;
import hr.prism.board.representation.UserRepresentation;
import hr.prism.board.service.UserService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.validation.Valid;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
public class UserApi {

    private final UserService userService;

    private final UserMapper userMapper;

    @Inject
    public UserApi(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @RequestMapping(value = "/api/user", method = GET)
    public UserRepresentation getUser() {
        return userMapper.apply(userService.getUserForRepresentation());
    }

    @RequestMapping(value = "/api/user", method = PATCH)
    public UserRepresentation updateUser(@RequestBody @Valid UserPatchDTO userDTO) {
        User currentUser = userService.updateUser(userDTO);
        return userMapper.apply(currentUser);
    }

    @RequestMapping(value = "/api/user/password", method = PATCH)
    public void resetPassword(@RequestBody @Valid UserPasswordDTO userPasswordDTO) {
        userService.resetPassword(userPasswordDTO);
    }

    @RequestMapping(value = "/api/user/test", method = DELETE)
    public void deleteTestUsers() {
        userService.deleteTestUsers();
    }

}
