package hr.prism.board.api;

import hr.prism.board.domain.User;
import hr.prism.board.dto.UserPatchDTO;
import hr.prism.board.mapper.UserMapper;
import hr.prism.board.representation.UserRepresentation;
import hr.prism.board.service.UserService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.validation.Valid;

@RestController(value = "/api")
public class UserApi {

    @Inject
    private UserService userService;

    @Inject
    private UserMapper userMapper;

    @RequestMapping(value = "/user", method = RequestMethod.GET)
    public UserRepresentation getCurrentUser() {
        User currentUser = userService.getCurrentUser();
        return userMapper.apply(currentUser);
    }

    @RequestMapping(value = "/user", method = RequestMethod.PATCH)
    public UserRepresentation updateUser(@RequestBody @Valid UserPatchDTO userDTO) {
        User currentUser = userService.updateUser(userDTO);
        return userMapper.apply(currentUser);
    }

}
