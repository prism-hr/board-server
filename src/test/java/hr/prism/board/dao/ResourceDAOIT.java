package hr.prism.board.dao;

import hr.prism.board.DBTestContext;
import hr.prism.board.enums.Scope;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;

import static hr.prism.board.enums.Scope.DEPARTMENT;

@DBTestContext
@RunWith(SpringRunner.class)
@Sql("classpath:data/resourceDAO_setUp.sql")
@Sql("classpath:data/resourceDAO_tearDown.sql")
public class ResourceDAOIT {

    @Inject
    private ResourceDAO resourceDAO;

    @Test
    public void checkUniqueName_success() {
        resourceDAO.checkUniqueName(DEPARTMENT, 1L, );
    }

    @Test
    public void checkUniqueName_failureWhenAlreadyExists() {

    }

}
