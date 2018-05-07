package hr.prism.board.service;

import hr.prism.board.DbTestContext;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;

@DbTestContext
@RunWith(SpringRunner.class)
public class UserServiceIT {

    @Inject
    private UserService userService;

    public void updateUserOrganizationAndLocation_success() {
        userService.updateUserOrganizationAndLocation(null, null, null);
    }

}
