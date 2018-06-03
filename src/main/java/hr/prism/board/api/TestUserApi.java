package hr.prism.board.api;

import hr.prism.board.service.TestUserService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;

@RestController
public class TestUserApi {

    private final TestUserService testUserService;

    @Inject
    public TestUserApi(TestUserService testUserService) {
        this.testUserService = testUserService;
    }

    @RequestMapping(value = "/api/user/test", method = DELETE)
    public void deleteTestUsers() {
        testUserService.deleteTestUsers();
    }

}
