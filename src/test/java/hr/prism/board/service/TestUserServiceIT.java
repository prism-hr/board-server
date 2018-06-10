package hr.prism.board.service;

import hr.prism.board.DbTestContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;

import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

@DbTestContext
@RunWith(SpringRunner.class)
@Sql(scripts = {"classpath:data/tearDown.sql", "classpath:data/testUserService_setUp.sql"})
@Sql(scripts = {"classpath:data/tearDown.sql"}, executionPhase = AFTER_TEST_METHOD)
public class TestUserServiceIT {

    @Inject
    private TestUserService testUserService;

    @Test
    public void shouldDeleteTestUsers() {
        testUserService.deleteTestUsers();
    }

}
