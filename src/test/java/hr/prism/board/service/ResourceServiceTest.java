package hr.prism.board.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import hr.prism.board.dao.ResourceDAO;
import hr.prism.board.domain.*;
import hr.prism.board.exception.BoardNotFoundException;
import hr.prism.board.repository.ResourceCategoryRepository;
import hr.prism.board.repository.ResourceOperationRepository;
import hr.prism.board.repository.ResourceRelationRepository;
import hr.prism.board.repository.ResourceRepository;
import hr.prism.board.value.ResourceFilter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;

import static hr.prism.board.enums.Scope.*;
import static hr.prism.board.enums.State.DRAFT;
import static hr.prism.board.enums.State.PREVIOUS;
import static hr.prism.board.exception.ExceptionCode.MISSING_RESOURCE;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.right;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
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
    public void updateState_success() {
        LocalDateTime baseline = LocalDateTime.now().minusSeconds(1L);

        Department department = new Department();
        resourceService.updateState(department, DRAFT);

        assertEquals(DRAFT, department.getState());
        assertEquals(DRAFT, department.getPreviousState());
        assertThat(department.getStateChangeTimestamp()).isGreaterThan(baseline);

        verify(resourceRepository, times(1)).save(department);
        verify(entityManager, times(1)).flush();
    }

    @Test
    public void updateState_failureWhenPrevious() {
        assertThatThrownBy(() -> resourceService.updateState(new Department(), PREVIOUS))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Previous state is anonymous - cannot be assigned to a resource");
    }

    @Test
    public void createResourceRelation_successWhenDepartment() {
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

        verify(entityManager, times(1)).flush();
        verify(entityManager, times(1)).refresh(university);
        verify(entityManager, times(1)).refresh(department);

        verify(resourceRelationRepository, times(1)).save(universityDepartmentRelation);
        verify(resourceRelationRepository, times(1)).save(departmentRelation);
    }

    @Test
    public void createResourceRelation_successWhenBoard() {
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

        verify(entityManager, times(1)).flush();
        verify(entityManager, times(1)).refresh(department);
        verify(entityManager, times(1)).refresh(board);

        verify(resourceRelationRepository, times(1)).save(universityBoardRelation);
        verify(resourceRelationRepository, times(1)).save(departmentBoardRelation);
        verify(resourceRelationRepository, times(1)).save(boardRelation);
    }

    @Test
    public void getResource_failureWhenResourceNotFound() {
        User user = new User();
        user.setId(1L);

        ResourceFilter filter =
            new ResourceFilter()
                .setScope(DEPARTMENT)
                .setId(1L);

        assertThatThrownBy(() -> resourceService.getResource(user, DEPARTMENT, 1L))
            .isExactlyInstanceOf(BoardNotFoundException.class)
            .hasFieldOrPropertyWithValue("exceptionCode", MISSING_RESOURCE)
            .hasFieldOrPropertyWithValue("properties",
                ImmutableMap.of("scope", DEPARTMENT, "id", 1L));

        verify(resourceDAO, times(1)).getResources(user, filter);
        verify(resourceDAO, times(1)).getById(DEPARTMENT, 1L);
    }

}
