package hr.prism.board.dao;

import hr.prism.board.DBTestContext;
import hr.prism.board.domain.Department;
import hr.prism.board.domain.University;
import hr.prism.board.exception.BoardDuplicateException;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;

import static hr.prism.board.enums.Scope.BOARD;
import static hr.prism.board.enums.Scope.DEPARTMENT;
import static hr.prism.board.exception.ExceptionCode.DUPLICATE_BOARD;
import static hr.prism.board.exception.ExceptionCode.DUPLICATE_DEPARTMENT;
import static java.util.Collections.singletonMap;
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
    public void checkUniqueName_failureWhenCreateDepartmentDuplicate() {
        University university = new University();
        university.setId(1L);
        Assertions.assertThatThrownBy(() ->
            resourceDAO.checkUniqueName(DEPARTMENT, null, university, "department", DUPLICATE_DEPARTMENT))
            .isExactlyInstanceOf(BoardDuplicateException.class)
            .hasMessage("DUPLICATE_DEPARTMENT: DEPARTMENT with name department exists already")
            .hasFieldOrPropertyWithValue("properties", singletonMap("id", 2L));
    }

    @Test
    public void checkUniqueName_successWhenCreateBoard() {
        University university = new University();
        university.setId(1L);
        resourceDAO.checkUniqueName(DEPARTMENT, null, university, "new board", DUPLICATE_DEPARTMENT);
    }

    @Test
    public void checkUniqueName_failureWhenCreateBoardDuplicate() {
        Department department = new Department();
        department.setId(2L);
        Assertions.assertThatThrownBy(() ->
            resourceDAO.checkUniqueName(BOARD, null, department, "board", DUPLICATE_BOARD))
            .isExactlyInstanceOf(BoardDuplicateException.class)
            .hasMessage("DUPLICATE_BOARD: BOARD with name board exists already")
            .hasFieldOrPropertyWithValue("properties", singletonMap("id", 3L));
    }

}
