package hr.prism.board.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import hr.prism.board.DbTestContext;
import hr.prism.board.dao.ResourceDAO;
import hr.prism.board.domain.*;
import hr.prism.board.enums.Scope;
import hr.prism.board.exception.BoardDuplicateException;
import hr.prism.board.exception.BoardNotFoundException;
import hr.prism.board.repository.ResourceRepository;
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
import static hr.prism.board.enums.CategoryType.MEMBER;
import static hr.prism.board.enums.CategoryType.POST;
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
    private ResourceRepository resourceRepository;

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
    public void checkUniqueName_success() {
        Department department = new Department();
        department.setParent(university);
        resourceService.checkUniqueName(department, "new department");

        Board board = new Board();
        board.setParent(this.department);
        resourceService.checkUniqueName(board, "new board");
    }

    @Test
    public void checkUniqueName_failureWhenDuplicate() {
        Department department = new Department();
        department.setParent(university);

        assertThatThrownBy(() -> resourceService.checkUniqueName(department, "department"))
            .isExactlyInstanceOf(BoardDuplicateException.class)
            .hasFieldOrPropertyWithValue("exceptionCode", DUPLICATE_RESOURCE)
            .hasFieldOrPropertyWithValue("properties", singletonMap("id", 2L));

        Board board = new Board();
        board.setParent(this.department);

        assertThatThrownBy(() -> resourceService.checkUniqueName(board, "board"))
            .isExactlyInstanceOf(BoardDuplicateException.class)
            .hasFieldOrPropertyWithValue("exceptionCode", DUPLICATE_RESOURCE)
            .hasFieldOrPropertyWithValue("properties", singletonMap("id", 4L));
    }

    @Test
    public void createHandle_success() {
        Department department = new Department();
        department.setParent(university);
        department.setName("department2");

        assertEquals("university/department2", resourceService.createHandle(department));
        department.setHandle("university/department2");

        Board board = new Board();
        board.setParent(department);
        board.setName("board2");

        assertEquals("university/department2/board2", resourceService.createHandle(board));
    }

    @Test
    public void createHandle_successWhenDuplicate() {
        Department department = new Department();
        department.setParent(university);
        department.setName("department");

        assertEquals("university/department-3", resourceService.createHandle(department));
        department.setHandle("university/department");

        Board board = new Board();
        board.setName("board");
        board.setParent(department);

        assertEquals("university/department/board-3", resourceService.createHandle(board));
    }

    @Test
    public void updateState_success() {
        resourceService.updateState(department, ACCEPTED);
        assertEquals(DRAFT, department.getPreviousState());
        assertEquals(ACCEPTED, department.getState());
        assertThat(department.getStateChangeTimestamp()).isGreaterThanOrEqualTo(baseline);
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

    @Test
    public void setIndexDataAndQuarter_success() {
        resourceService.setIndexDataAndQuarter(university);
        resourceRepository.save(university);

        Department department = (Department) resourceService.getById(2L);
        resourceService.setIndexDataAndQuarter(department);
        resourceRepository.save(department);

        Board board = (Board) resourceService.getById(4L);
        resourceService.setIndexDataAndQuarter(board);
        resourceRepository.save(board);

        Post post = (Post) resourceService.getById(6L);
        resourceService.setIndexDataAndQuarter(post);

        assertEquals("U516", university.getIndexData());
        assertEquals("U516 D163 D163 S560", department.getIndexData());
        assertEquals("U516 D163 D163 S560 B630", board.getIndexData());
        assertEquals("U516 D163 D163 S560 B630 P230 P230 S560 P230 D261 L535 O625 N500", post.getIndexData());

        Stream.of(university, department, board, post).forEach(resource ->
            resource.setCreatedTimestamp(LocalDateTime.of(2018, 5, 1, 0, 0, 0)));

        assertEquals("20182", university.getQuarter());
        assertEquals("20182", department.getQuarter());
        assertEquals("20182", board.getQuarter());
        assertEquals("20182", post.getQuarter());
    }

    @Test
    public void updateCategories_success() {
        resourceService.updateCategories(department, MEMBER, ImmutableList.of("UNDERGRADUATE_STUDENT"));

        Department selectedDepartment = (Department) resourceDAO.getById(DEPARTMENT, 2L);
        assertThat(selectedDepartment.getMemberCategoryStrings()).containsExactly("UNDERGRADUATE_STUDENT");

        Board board = (Board) resourceService.getById(4L);
        resourceService.updateCategories(board, POST, null);

        Board selectedBoard = (Board) resourceDAO.getById(BOARD, 4L);
        assertThat(selectedBoard.getPostCategoryStrings()).isEmpty();

        Post post = (Post) resourceService.getById(6L);
        resourceService.updateCategories(post, POST, ImmutableList.of("Internship", "Volunteering"));
        resourceService.updateCategories(post, MEMBER, ImmutableList.of("MASTER_STUDENT", "RESEARCH_STUDENT"));

        Post selectedPost = (Post) resourceDAO.getById(Scope.POST, 6L);
        assertThat(selectedPost.getPostCategoryStrings()).containsExactly("Internship", "Volunteering");
        assertThat(selectedPost.getMemberCategoryStrings()).containsExactly("MASTER_STUDENT", "RESEARCH_STUDENT");
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
