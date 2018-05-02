package hr.prism.board.service;

import com.google.common.collect.ImmutableMap;
import hr.prism.board.DbTestContext;
import hr.prism.board.dao.ResourceDAO;
import hr.prism.board.domain.Department;
import hr.prism.board.domain.ResourceOperation;
import hr.prism.board.domain.University;
import hr.prism.board.domain.User;
import hr.prism.board.exception.BoardDuplicateException;
import hr.prism.board.exception.BoardNotFoundException;
import hr.prism.board.repository.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.time.LocalDateTime;

import static hr.prism.board.enums.Action.EXTEND;
import static hr.prism.board.enums.Scope.BOARD;
import static hr.prism.board.enums.Scope.DEPARTMENT;
import static hr.prism.board.exception.ExceptionCode.DUPLICATE_RESOURCE;
import static hr.prism.board.exception.ExceptionCode.MISSING_RESOURCE;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

@DbTestContext
@RunWith(SpringRunner.class)
@Sql("classpath:data/resourceService_setUp.sql")
@Sql(value = "classpath:data/resourceService_tearDown.sql", executionPhase = AFTER_TEST_METHOD)
public class ResourceServiceIT {

    @Inject
    private UserRepository userRepository;

    @Inject
    private ResourceDAO resourceDAO;

    @Inject
    private ResourceService resourceService;

    @Inject
    private ServiceHelper serviceHelper;

    private User user;

    private University university;

    private Department department;

    @Before
    public void setUp() {
        user = userRepository.findOne(1L);
        university = (University) resourceService.getById(1L);
        department = (Department) resourceService.getById(2L);
    }

    @Test
    public void getResource_failureWhenIdNotFound() {
        assertThatThrownBy(() -> resourceService.getResource(user, DEPARTMENT, 0L))
            .isExactlyInstanceOf(BoardNotFoundException.class)
            .hasFieldOrPropertyWithValue("exceptionCode", MISSING_RESOURCE)
            .hasFieldOrPropertyWithValue("properties",
                ImmutableMap.of("scope", DEPARTMENT, "id", 0L));
    }

    @Test
    public void getResource_failureWhenHandleNotFound() {
        assertThatThrownBy(() -> resourceService.getResource(user, DEPARTMENT, "university/department-3"))
            .isExactlyInstanceOf(BoardNotFoundException.class)
            .hasFieldOrPropertyWithValue("exceptionCode", MISSING_RESOURCE)
            .hasFieldOrPropertyWithValue("properties",
                ImmutableMap.of("scope", DEPARTMENT, "handle", "university/department-3"));
    }

    @Test
    public void checkUniqueName_successWhenCreateDepartment() {
        resourceService.checkUniqueName(DEPARTMENT, null, university, "new department");
    }

    @Test
    public void checkUniqueName_failureWhenCreateDepartmentDuplicate() {
        assertThatThrownBy(() -> resourceService.checkUniqueName(DEPARTMENT, null, university, "department"))
            .isExactlyInstanceOf(BoardDuplicateException.class)
            .hasFieldOrPropertyWithValue("exceptionCode", DUPLICATE_RESOURCE)
            .hasFieldOrPropertyWithValue("properties", singletonMap("id", 2L));
    }

    @Test
    public void checkUniqueName_successWhenCreateBoard() {
        resourceService.checkUniqueName(DEPARTMENT, null, university, "new board");
    }

    @Test
    public void checkUniqueName_failureWhenCreateBoardDuplicate() {
        Department department = new Department();
        department.setId(2L);
        assertThatThrownBy(() -> resourceService.checkUniqueName(BOARD, null, department, "board"))
            .isExactlyInstanceOf(BoardDuplicateException.class)
            .hasFieldOrPropertyWithValue("exceptionCode", DUPLICATE_RESOURCE)
            .hasFieldOrPropertyWithValue("properties", singletonMap("id", 4L));
    }

    @Test
    public void createHandle_successWhenDepartment() {
        String handle = resourceService.createHandle(university, DEPARTMENT, "department2");
        assertEquals("university/department2", handle);
    }

    @Test
    public void createHandle_successWhenDepartmentAndDuplicate() {
        String handle = resourceService.createHandle(university, DEPARTMENT, "department");
        assertEquals("university/department-3", handle);
    }

    @Test
    public void createHandle_successWhenBoard() {
        String handle = resourceService.createHandle(department, BOARD, "board2");
        assertEquals("university/department/board2", handle);
    }

    @Test
    public void createHandle_successWhenBoardAndDuplicate() {
        String handle = resourceService.createHandle(department, BOARD, "board");
        assertEquals("university/department/board-3", handle);
    }

    @Test
    public void createResourceOperation_success() {
        LocalDateTime baseline = LocalDateTime.now();
        ResourceOperation resourceOperation = resourceService.createResourceOperation(department, EXTEND, user);

        assertNotNull(resourceOperation.getId());
        assertEquals(department, resourceOperation.getResource());
        assertEquals(EXTEND, resourceOperation.getAction());
        assertEquals(user, resourceOperation.getUser());

        serviceHelper.verifyTimestamps(resourceOperation, baseline);
        assertThat(resourceDAO.getResourceOperations(department)).containsExactly(resourceOperation);
    }

}
