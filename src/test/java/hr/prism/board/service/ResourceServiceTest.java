package hr.prism.board.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import hr.prism.board.dao.ResourceDAO;
import hr.prism.board.domain.*;
import hr.prism.board.repository.ResourceCategoryRepository;
import hr.prism.board.repository.ResourceOperationRepository;
import hr.prism.board.repository.ResourceRelationRepository;
import hr.prism.board.repository.ResourceRepository;
import hr.prism.board.value.ResourceFilter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;

import static hr.prism.board.enums.Action.EXTEND;
import static hr.prism.board.enums.CategoryType.MEMBER;
import static hr.prism.board.enums.CategoryType.POST;
import static hr.prism.board.enums.Scope.*;
import static hr.prism.board.enums.State.*;
import static hr.prism.board.exception.ExceptionCode.DUPLICATE_BOARD;
import static hr.prism.board.exception.ExceptionCode.DUPLICATE_DEPARTMENT;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.right;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ResourceServiceTest {

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private ResourceDAO resourceDAO;

    @Mock
    private ResourceRelationRepository resourceRelationRepository;

    @Mock
    private ResourceCategoryRepository resourceCategoryRepository;

    @Mock
    private ResourceOperationRepository resourceOperationRepository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private ObjectMapper objectMapper;

    private ResourceService resourceService;

    @Before
    public void setUp() {
        when(resourceCategoryRepository.save(any(ResourceCategory.class))).thenAnswer(invocation -> {
            ResourceCategory resourceCategory = (ResourceCategory) invocation.getArguments()[0];
            String name = resourceCategory.getName();
            resourceCategory.setId(Long.parseLong(right(name, 1)));
            return resourceCategory;
        });

        resourceService = new ResourceService(1L, resourceRepository, resourceDAO,
            resourceRelationRepository, resourceCategoryRepository, resourceOperationRepository, entityManager,
            objectMapper);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(resourceRepository, resourceDAO, resourceRelationRepository,
            resourceCategoryRepository, resourceOperationRepository, entityManager, objectMapper);
        reset(resourceRepository, resourceDAO, resourceRelationRepository,
            resourceCategoryRepository, resourceOperationRepository, entityManager, objectMapper);
    }

    @Test
    public void getById_success() {
        resourceService.getById(1L);
        verify(resourceRepository, times(1)).findOne(1L);
    }

    @Test
    public void checkNameUnique_successWhenCreateDepartment() {
        resourceService.checkUniqueName(
            DEPARTMENT, null, new University(), "department", DUPLICATE_DEPARTMENT);
        verify(resourceDAO, times(1)).checkUniqueName(
            DEPARTMENT, null, new University(), "department", DUPLICATE_DEPARTMENT);
    }

    @Test
    public void checkNameUnique_successWhenCreateBoard() {
        resourceService.checkUniqueName(
            BOARD, null, new Department(), "board", DUPLICATE_BOARD);
        verify(resourceDAO, times(1)).checkUniqueName(
            BOARD, null, new Department(), "board", DUPLICATE_BOARD);
    }

    @Test
    public void createHandle_successWhenDepartment() {
        when(resourceRepository.findHandleLikeSuggestedHandle(DEPARTMENT, "university/department"))
            .thenReturn(emptyList());

        University university = new University();
        university.setId(1L);
        university.setHandle("university");

        String handle = resourceService.createHandle(university, DEPARTMENT, "department");
        assertEquals("university/department", handle);

        verify(resourceRepository, times(1))
            .findHandleLikeSuggestedHandle(DEPARTMENT, "university/department");
    }

    @Test
    public void createHandle_successWhenDepartmentAndSimilar() {
        when(resourceRepository.findHandleLikeSuggestedHandle(DEPARTMENT, "university/department"))
            .thenReturn(ImmutableList.of("university/department-2"));

        University university = new University();
        university.setId(1L);
        university.setHandle("university");

        String handle = resourceService.createHandle(university, DEPARTMENT, "department");
        assertEquals("university/department", handle);

        verify(resourceRepository, times(1))
            .findHandleLikeSuggestedHandle(DEPARTMENT, "university/department");
    }

    @Test
    public void createHandle_successWhenDepartmentAndDuplicate() {
        when(resourceRepository.findHandleLikeSuggestedHandle(DEPARTMENT, "university/department"))
            .thenReturn(ImmutableList.of("university/department"));

        University university = new University();
        university.setId(1L);
        university.setHandle("university");

        String handle = resourceService.createHandle(university, DEPARTMENT, "department");
        assertEquals("university/department-2", handle);

        verify(resourceRepository, times(1))
            .findHandleLikeSuggestedHandle(DEPARTMENT, "university/department");
    }

    @Test
    public void createHandle_successWhenBoard() {
        when(resourceRepository.findHandleLikeSuggestedHandle(BOARD, "university/department/board"))
            .thenReturn(emptyList());

        Department department = new Department();
        department.setId(1L);
        department.setHandle("university/department");

        String handle = resourceService.createHandle(department, BOARD, "board");
        assertEquals("university/department/board", handle);

        verify(resourceRepository, times(1))
            .findHandleLikeSuggestedHandle(BOARD, "university/department/board");
    }

    @Test
    public void createHandle_successWhenBoardAndSimilar() {
        when(resourceRepository.findHandleLikeSuggestedHandle(BOARD, "university/department/board"))
            .thenReturn(ImmutableList.of("university/department/board-2"));

        Department department = new Department();
        department.setId(1L);
        department.setHandle("university/department");

        String handle = resourceService.createHandle(department, BOARD, "board");
        assertEquals("university/department/board", handle);

        verify(resourceRepository, times(1))
            .findHandleLikeSuggestedHandle(BOARD, "university/department/board");
    }

    @Test
    public void createHandle_successWhenBoardAndDuplicate() {
        when(resourceRepository.findHandleLikeSuggestedHandle(BOARD, "university/department/board"))
            .thenReturn(ImmutableList.of("university/department/board"));

        Department department = new Department();
        department.setId(1L);
        department.setHandle("university/department");

        String handle = resourceService.createHandle(department, BOARD, "board");
        assertEquals("university/department/board-2", handle);

        verify(resourceRepository, times(1))
            .findHandleLikeSuggestedHandle(BOARD, "university/department/board");
    }

    @Test
    public void updateState_successWhenDepartment() {
        LocalDateTime baseline = LocalDateTime.now().minusSeconds(1L);

        Department department = new Department();
        resourceService.updateState(department, DRAFT);

        assertEquals(DRAFT, department.getState());
        assertEquals(DRAFT, department.getPreviousState());
        assertThat(department.getStateChangeTimestamp()).isGreaterThan(baseline);

        verify(entityManager, times(1)).flush();
    }

    @Test
    public void updateState_failureWhenDepartmentAndPrevious() {
        assertThatThrownBy(() -> resourceService.updateState(new Department(), PREVIOUS))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Previous state is anonymous - cannot be assigned to a resource");
    }

    @Test
    public void updateState_successWhenBoard() {
        LocalDateTime baseline = LocalDateTime.now().minusSeconds(1L);

        Board board = new Board();
        resourceService.updateState(board, ACCEPTED);

        assertEquals(ACCEPTED, board.getState());
        assertEquals(ACCEPTED, board.getPreviousState());
        assertThat(board.getStateChangeTimestamp()).isGreaterThan(baseline);

        verify(entityManager, times(1)).flush();
    }

    @Test
    public void updateState_failureWhenBoardAndPrevious() {
        assertThatThrownBy(() -> resourceService.updateState(new Board(), PREVIOUS))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Previous state is anonymous - cannot be assigned to a resource");
    }

    @Test
    public void updateCategories_successWhenCreateDepartmentAndMember() {
        Department department = new Department();
        department.setId(1L);

        ResourceCategory category1 = new ResourceCategory();
        category1.setResource(department);
        category1.setType(MEMBER);
        category1.setName("category1");

        ResourceCategory category2 = new ResourceCategory();
        category2.setResource(department);
        category2.setType(MEMBER);
        category2.setName("category2");

        resourceService.updateCategories(department, MEMBER, ImmutableList.of("category1", "category2"));
        assertThat(department.getCategories(MEMBER)).containsExactly(category1, category2);

        verify(resourceCategoryRepository, times(1)).deleteByResourceAndType(department, MEMBER);
        verify(resourceCategoryRepository, times(1)).save(category1);
        verify(resourceCategoryRepository, times(1)).save(category2);
    }

    @Test
    public void updateCategories_successWhenCreateBoardAndPost() {
        Board board = new Board();
        board.setId(1L);

        ResourceCategory category1 = new ResourceCategory();
        category1.setResource(board);
        category1.setType(POST);
        category1.setName("category1");

        ResourceCategory category2 = new ResourceCategory();
        category2.setResource(board);
        category2.setType(POST);
        category2.setName("category2");

        resourceService.updateCategories(board, POST, ImmutableList.of("category1", "category2"));
        assertThat(board.getCategories(POST)).containsExactly(category1, category2);

        verify(resourceCategoryRepository, times(1)).deleteByResourceAndType(board, POST);
        verify(resourceCategoryRepository, times(1)).save(category1);
        verify(resourceCategoryRepository, times(1)).save(category2);
    }

    @Test
    public void createResourceRelation_successWhenCreateDepartment() {
        University university = new University();
        university.setScope(UNIVERSITY);
        university.setId(1L);
        university.setParent(university);

        Department department = new Department();
        department.setScope(DEPARTMENT);
        department.setId(2L);

        ResourceRelation universityRelation = new ResourceRelation();
        universityRelation.setResource1(university);
        universityRelation.setResource2(university);

        ResourceRelation universityDepartmentRelation = new ResourceRelation();
        universityDepartmentRelation.setResource1(university);
        universityDepartmentRelation.setResource2(department);

        ResourceRelation departmentRelation = new ResourceRelation();
        departmentRelation.setResource1(department);
        departmentRelation.setResource2(department);

        university.getParents().add(universityRelation);
        university.getChildren().add(universityRelation);

        resourceService.createResourceRelation(university, department);
        assertEquals(university, department.getParent());

        assertThat(university.getChildren()).containsExactly(universityRelation, universityDepartmentRelation);

        assertThat(department.getParents()).containsExactlyInAnyOrder(universityDepartmentRelation, departmentRelation);
        assertThat(department.getChildren()).containsExactly(departmentRelation);

        verify(entityManager, times(1)).flush();
        verify(entityManager, times(1)).refresh(university);
        verify(entityManager, times(1)).refresh(department);

        verify(resourceRelationRepository, times(1)).save(universityDepartmentRelation);
        verify(resourceRelationRepository, times(1)).save(departmentRelation);
    }

    @Test
    public void createResourceRelation_successWhenCreateBoard() {
        University university = new University();
        university.setScope(UNIVERSITY);
        university.setId(1L);
        university.setParent(university);

        Department department = new Department();
        department.setScope(DEPARTMENT);
        department.setId(2L);
        department.setParent(university);

        Board board = new Board();
        board.setScope(BOARD);
        board.setId(3L);

        ResourceRelation universityRelation = new ResourceRelation();
        universityRelation.setResource1(university);
        universityRelation.setResource2(university);

        ResourceRelation universityDepartmentRelation = new ResourceRelation();
        universityDepartmentRelation.setResource1(university);
        universityDepartmentRelation.setResource2(department);

        ResourceRelation universityBoardRelation = new ResourceRelation();
        universityBoardRelation.setResource1(university);
        universityBoardRelation.setResource2(board);

        ResourceRelation departmentRelation = new ResourceRelation();
        departmentRelation.setResource1(department);
        departmentRelation.setResource2(department);

        ResourceRelation departmentBoardRelation = new ResourceRelation();
        departmentBoardRelation.setResource1(department);
        departmentBoardRelation.setResource2(board);

        ResourceRelation boardRelation = new ResourceRelation();
        boardRelation.setResource1(board);
        boardRelation.setResource2(board);

        university.getParents().add(universityRelation);
        university.getChildren().add(universityRelation);
        university.getChildren().add(universityDepartmentRelation);

        department.getParents().add(universityDepartmentRelation);
        department.getParents().add(departmentRelation);
        department.getChildren().add(departmentRelation);

        resourceService.createResourceRelation(department, board);
        assertEquals(department, board.getParent());

        assertThat(university.getChildren())
            .containsExactlyInAnyOrder(universityRelation, universityDepartmentRelation, universityBoardRelation);
        assertThat(department.getChildren()).containsExactly(departmentRelation, departmentBoardRelation);

        assertThat(board.getParents())
            .containsExactlyInAnyOrder(universityBoardRelation, departmentBoardRelation, boardRelation);
        assertThat(board.getChildren()).containsExactly(boardRelation);

        verify(entityManager, times(1)).flush();
        verify(entityManager, times(1)).refresh(department);
        verify(entityManager, times(1)).refresh(board);

        verify(resourceRelationRepository, times(1)).save(universityBoardRelation);
        verify(resourceRelationRepository, times(1)).save(departmentBoardRelation);
        verify(resourceRelationRepository, times(1)).save(boardRelation);
    }


    @Test
    public void setIndexDataAndQuarter_successWhenDepartment() {
        University university = new University();
        university.setName("university");
        university.setIndexData("U516");

        Department department = new Department();
        department.setParent(university);
        department.setName("department");
        department.setSummary("department summary");
        department.setCreatedTimestamp(LocalDateTime.of(2018, 4, 20, 9, 0, 0));

        resourceService.setIndexDataAndQuarter(department);
        assertEquals("U516 D163 D163 S560", department.getIndexData());
        assertEquals("20182", department.getQuarter());
    }

    @Test
    public void setIndexDataAndQuarter_successWhenBoard() {
        Department department = new Department();
        department.setName("department");
        department.setSummary("department summary");
        department.setIndexData("U516 D163 D163 S560");

        Board board = new Board();
        board.setParent(department);
        board.setName("board");
        board.setSummary("board summary");
        board.setCreatedTimestamp(LocalDateTime.of(2018, 4, 20, 9, 0, 0));

        resourceService.setIndexDataAndQuarter(board);
        assertEquals("U516 D163 D163 S560 B630 B630 S560", board.getIndexData());
        assertEquals("20182", board.getQuarter());
    }

    @Test
    public void createResourceOperation_successWhenDepartment() {
        Department department = new Department();
        department.setId(1L);

        User user = new User();
        user.setId(1L);

        when(resourceOperationRepository.save(any(ResourceOperation.class)))
            .thenAnswer(invocation -> {
                ResourceOperation operation = (ResourceOperation) invocation.getArguments()[0];
                operation.setId(1L);
                return operation;
            });

        resourceService.createResourceOperation(department, EXTEND, user);

        ResourceOperation operation = new ResourceOperation();
        operation.setId(1L);

        assertThat(department.getOperations()).containsExactly(operation);

        verify(resourceOperationRepository, times(1))
            .save(argThat(
                new ArgumentMatcher<ResourceOperation>() {

                    @Override
                    public boolean matches(Object argument) {
                        ResourceOperation operation = (ResourceOperation) argument;
                        return department.equals(operation.getResource())
                            && EXTEND == operation.getAction()
                            && user.equals(operation.getUser());
                    }

                }));

        verify(resourceRepository, times(1)).save(department);
    }

    @Test
    public void createResourceOperation_successWhenBoard() {
        Board board = new Board();
        board.setId(1L);

        User user = new User();
        user.setId(1L);

        when(resourceOperationRepository.save(any(ResourceOperation.class)))
            .thenAnswer(invocation -> {
                ResourceOperation operation = (ResourceOperation) invocation.getArguments()[0];
                operation.setId(1L);
                return operation;
            });

        resourceService.createResourceOperation(board, EXTEND, user);

        ResourceOperation operation = new ResourceOperation();
        operation.setId(1L);

        assertThat(board.getOperations()).containsExactly(operation);

        verify(resourceOperationRepository, times(1))
            .save(argThat(
                new ArgumentMatcher<ResourceOperation>() {

                    @Override
                    public boolean matches(Object argument) {
                        ResourceOperation operation = (ResourceOperation) argument;
                        return board.equals(operation.getResource())
                            && EXTEND == operation.getAction()
                            && user.equals(operation.getUser());
                    }

                }));

        verify(resourceRepository, times(1)).save(board);
    }

    @Test
    public void getResource_successWhenDepartment() {
        User user = new User();
        user.setId(1L);

        ResourceFilter filter =
            new ResourceFilter()
                .setScope(DEPARTMENT)
                .setId(1L)
                .setIncludePublicResources(true);

        when(resourceDAO.getResources(user, filter)).thenReturn(ImmutableList.of(new Department()));

        assertNotNull(resourceService.getResource(user, DEPARTMENT, 1L));

        verify(resourceDAO, times(1)).getResources(user, filter);
    }

    @Test
    public void getResource_successWhenDepartmentWithNoActions() {
        User user = new User();
        user.setId(1L);

        ResourceFilter filter =
            new ResourceFilter()
                .setScope(DEPARTMENT)
                .setId(1L)
                .setIncludePublicResources(true);

        when(resourceDAO.getResources(user, filter)).thenReturn(emptyList());
        when(resourceRepository.findOne(1L)).thenReturn(new Department());

        assertNotNull(resourceService.getResource(user, DEPARTMENT, 1L));

        verify(resourceDAO, times(1)).getResources(user, filter);
        verify(resourceRepository, times(1)).findOne(1L);
    }

    @Test
    public void getResource_successWhenBoard() {
        User user = new User();
        user.setId(1L);

        ResourceFilter filter =
            new ResourceFilter()
                .setScope(BOARD)
                .setId(1L)
                .setIncludePublicResources(true);

        when(resourceDAO.getResources(user, filter)).thenReturn(ImmutableList.of(new Board()));

        assertNotNull(resourceService.getResource(user, BOARD, 1L));

        verify(resourceDAO, times(1)).getResources(user, filter);
    }

    @Test
    public void getResource_successWhenBoardWithNoActions() {
        User user = new User();
        user.setId(1L);

        ResourceFilter filter =
            new ResourceFilter()
                .setScope(BOARD)
                .setId(1L)
                .setIncludePublicResources(true);

        when(resourceDAO.getResources(user, filter)).thenReturn(emptyList());
        when(resourceRepository.findOne(1L)).thenReturn(new Board());

        assertNotNull(resourceService.getResource(user, BOARD, 1L));

        verify(resourceDAO, times(1)).getResources(user, filter);
        verify(resourceRepository, times(1)).findOne(1L);
    }

}
