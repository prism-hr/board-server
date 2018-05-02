package hr.prism.board.service;

import com.google.common.collect.ImmutableMap;
import hr.prism.board.DbTestContext;
import hr.prism.board.dao.ResourceDAO;
import hr.prism.board.domain.*;
import hr.prism.board.exception.BoardDuplicateException;
import hr.prism.board.exception.BoardNotFoundException;
import hr.prism.board.repository.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static hr.prism.board.enums.Action.EXTEND;
import static hr.prism.board.enums.Scope.BOARD;
import static hr.prism.board.enums.Scope.DEPARTMENT;
import static hr.prism.board.enums.State.*;
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

    @Inject
    private PlatformTransactionManager platformTransactionManager;

    private LocalDateTime baseline;

    private User user;

    private University university;

    private Department department;

    @Before
    public void setUp() {
        baseline = LocalDateTime.now();
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
    public void updateState_success() {
        resourceService.updateState(department, ACCEPTED);
        assertEquals(DRAFT, department.getPreviousState());
        assertEquals(ACCEPTED, department.getState());
        assertThat(department.getStateChangeTimestamp()).isGreaterThan(baseline);
    }

    @Test
    public void updateState_failureWhenPrevious() {
        assertThatThrownBy(() -> resourceService.updateState(department, PREVIOUS))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Previous state is anonymous - cannot be assigned to a resource");
    }

    @Test
    public void createResourceRelation_success() {
        createResourceRelation(1L, 2L);
        createResourceRelation(2L, 4L);
        createResourceRelation(4L, 6L);

        new TransactionTemplate(platformTransactionManager).execute(status -> {
            University university = (University) resourceService.getById(1L);
            Department department = (Department) resourceService.getById(2L);
            Board board = (Board) resourceService.getById(4L);
            Post post = (Post) resourceService.getById(6L);

            assertThat(university.getParents()).containsExactly(
                new ResourceRelation().setResource1(university).setResource2(university));

            assertThat(department.getParents()).containsExactlyInAnyOrder(
                new ResourceRelation().setResource1(university).setResource2(department),
                new ResourceRelation().setResource1(department).setResource2(department));

            assertThat(board.getParents()).containsExactlyInAnyOrder(
                new ResourceRelation().setResource1(university).setResource2(board),
                new ResourceRelation().setResource1(department).setResource2(board),
                new ResourceRelation().setResource1(board).setResource2(board));

            assertThat(post.getParents()).containsExactlyInAnyOrder(
                new ResourceRelation().setResource1(university).setResource2(post),
                new ResourceRelation().setResource1(department).setResource2(post),
                new ResourceRelation().setResource1(board).setResource2(post),
                new ResourceRelation().setResource1(post).setResource2(post));

            assertThat(university.getChildren()).containsExactlyInAnyOrder(
                new ResourceRelation().setResource1(university).setResource2(university),
                new ResourceRelation().setResource1(university).setResource2(department),
                new ResourceRelation().setResource1(university).setResource2(board),
                new ResourceRelation().setResource1(university).setResource2(post));

            assertThat(department.getChildren()).containsExactlyInAnyOrder(
                new ResourceRelation().setResource1(department).setResource2(department),
                new ResourceRelation().setResource1(department).setResource2(board),
                new ResourceRelation().setResource1(department).setResource2(post));

            assertThat(board.getChildren()).containsExactlyInAnyOrder(
                new ResourceRelation().setResource1(board).setResource2(board),
                new ResourceRelation().setResource1(board).setResource2(post));

            assertThat(post.getChildren()).containsExactly(
                new ResourceRelation().setResource1(post).setResource2(post));

            Stream.of(department, board, post).forEach(resource -> {
                resource.getParents().forEach(resourceRelation ->
                    serviceHelper.verifyTimestamps(resourceRelation, baseline));

                resource.getChildren().forEach(resourceRelation ->
                    serviceHelper.verifyTimestamps(resourceRelation, baseline));
            });

            return null;
        });
    }

    @Test
    public void createResourceOperation_success() {
        ResourceOperation resourceOperation = resourceService.createResourceOperation(department, EXTEND, user);

        assertNotNull(resourceOperation.getId());
        assertEquals(department, resourceOperation.getResource());
        assertEquals(EXTEND, resourceOperation.getAction());
        assertEquals(user, resourceOperation.getUser());

        serviceHelper.verifyTimestamps(resourceOperation, baseline);
        assertThat(resourceDAO.getResourceOperations(department)).containsExactly(resourceOperation);
    }

    private void createResourceRelation(Long resource1Id, Long resource2Id) {
        new TransactionTemplate(platformTransactionManager).execute(status -> {
            Resource resource1 = resourceService.getById(resource1Id);
            Resource resource2 = resourceService.getById(resource2Id);
            resourceService.createResourceRelation(resource1, resource2);
            return null;
        });
    }

}
