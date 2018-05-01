package hr.prism.board.service;

import hr.prism.board.DbTestContext;
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
import static hr.prism.board.exception.ExceptionCode.DUPLICATE_RESOURCE;
import static java.util.Collections.singletonMap;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

@DbTestContext
@RunWith(SpringRunner.class)
@Sql("classpath:data/resourceService_setUp.sql")
@Sql(value = "classpath:data/resourceService_tearDown.sql", executionPhase = AFTER_TEST_METHOD)
public class ResourceServiceIT {

    @Inject
    private ResourceService resourceService;

    @Test
    public void checkUniqueName_successWhenCreateDepartment() {
        University university = new University();
        university.setId(1L);
        resourceService.checkUniqueName(DEPARTMENT, null, university, "new department");
    }

    @Test
    public void checkUniqueName_failureWhenCreateDepartmentDuplicate() {
        University university = new University();
        university.setId(1L);
        Assertions.assertThatThrownBy(() ->
            resourceService.checkUniqueName(DEPARTMENT, null, university, "department"))
            .isExactlyInstanceOf(BoardDuplicateException.class)
            .hasFieldOrPropertyWithValue("exceptionCode", DUPLICATE_RESOURCE)
            .hasFieldOrPropertyWithValue("properties", singletonMap("id", 2L));
    }

    @Test
    public void checkUniqueName_successWhenCreateBoard() {
        University university = new University();
        university.setId(1L);
        resourceService.checkUniqueName(DEPARTMENT, null, university, "new board");
    }

    @Test
    public void checkUniqueName_failureWhenCreateBoardDuplicate() {
        Department department = new Department();
        department.setId(2L);
        Assertions.assertThatThrownBy(() ->
            resourceService.checkUniqueName(BOARD, null, department, "board"))
            .isExactlyInstanceOf(BoardDuplicateException.class)
            .hasFieldOrPropertyWithValue("exceptionCode", DUPLICATE_RESOURCE)
            .hasFieldOrPropertyWithValue("properties", singletonMap("id", 3L));
    }

}
