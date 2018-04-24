package hr.prism.board.dao;

import hr.prism.board.DBTestContext;
import hr.prism.board.domain.University;
import hr.prism.board.exception.BoardDuplicateException;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;

import static hr.prism.board.enums.Scope.DEPARTMENT;
import static hr.prism.board.exception.ExceptionCode.DUPLICATE_DEPARTMENT;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

@DBTestContext
@RunWith(SpringRunner.class)
@Sql("classpath:data/resourceDAO_setUp.sql")
@Sql(value = "classpath:data/resourceDAO_tearDown.sql", executionPhase = AFTER_TEST_METHOD)
public class ResourceDAOIT {

    @Inject
    private ResourceDAO resourceDAO;

    @Test
    public void checkUniqueName_successWhenCreateDepartment() {
        University university = new University();
        university.setId(1L);
        resourceDAO.checkUniqueName(DEPARTMENT, null, university, "new department", DUPLICATE_DEPARTMENT);
    }

    @Test
    public void checkUniqueName_failureWhenCreateDepartmentAlreadyExists() {
        University university = new University();
        university.setId(1L);
        Assertions.assertThatThrownBy(() ->
            resourceDAO.checkUniqueName(DEPARTMENT, null, university, "department", DUPLICATE_DEPARTMENT))
            .isExactlyInstanceOf(BoardDuplicateException.class)
            .hasMessage("DUPLICATE_DEPARTMENT: DEPARTMENT with name department exists already")
            .hasFieldOrPropertyWithValue("id", 2L);
    }

}
