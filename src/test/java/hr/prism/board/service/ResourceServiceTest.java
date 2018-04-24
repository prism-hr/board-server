package hr.prism.board.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import hr.prism.board.dao.ResourceDAO;
import hr.prism.board.domain.Department;
import hr.prism.board.domain.ResourceCategory;
import hr.prism.board.domain.ResourceRelation;
import hr.prism.board.domain.University;
import hr.prism.board.repository.ResourceCategoryRepository;
import hr.prism.board.repository.ResourceOperationRepository;
import hr.prism.board.repository.ResourceRelationRepository;
import hr.prism.board.repository.ResourceRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;

import static hr.prism.board.enums.CategoryType.MEMBER;
import static hr.prism.board.enums.Scope.DEPARTMENT;
import static hr.prism.board.enums.Scope.UNIVERSITY;
import static hr.prism.board.enums.State.DRAFT;
import static hr.prism.board.enums.State.PREVIOUS;
import static hr.prism.board.exception.ExceptionCode.DUPLICATE_DEPARTMENT;
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
    public void createHandle_successWhenCreateDepartment() {
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
    public void createHandle_successWhenCreateDepartmentAndSimilar() {
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
    public void createHandle_successWhenCreateDepartmentAndDuplicate() {
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
    public void updateState_successWhenCreateDepartment() {
        LocalDateTime baseline = LocalDateTime.now().minusSeconds(1L);

        Department department = new Department();
        resourceService.updateState(department, DRAFT);

        assertEquals(DRAFT, department.getState());
        assertEquals(DRAFT, department.getPreviousState());
        assertThat(department.getStateChangeTimestamp()).isGreaterThan(baseline);

        verify(entityManager, times(1)).flush();
    }

    @Test
    public void updateState_failureWhenCreateDepartmentAndPrevious() {
        assertThatThrownBy(() -> resourceService.updateState(new Department(), PREVIOUS))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Previous state is anonymous - cannot be assigned to a resource");
    }

    @Test
    public void updateCategories_successWhenCreateDepartmentAndMember() {
        when(resourceCategoryRepository.save(any(ResourceCategory.class))).thenAnswer(invocation -> {
            ResourceCategory resourceCategory = (ResourceCategory) invocation.getArguments()[0];
            String name = resourceCategory.getName();
            resourceCategory.setId(Long.parseLong(right(name, 1)));
            return resourceCategory;
        });

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
    public void createResourceRelation_successWhenCreateDepartment() {
        University university = new University();
        university.setScope(UNIVERSITY);
        university.setId(1L);

        ResourceRelation universityRelation = new ResourceRelation();
        universityRelation.setResource1(university);
        universityRelation.setResource2(university);

        university.getParents().add(universityRelation);
        university.getChildren().add(universityRelation);

        Department department = new Department();
        department.setScope(DEPARTMENT);
        department.setId(2L);

        resourceService.createResourceRelation(university, department);
        assertEquals(university, department.getParent());

        ResourceRelation universityDepartmentRelation = new ResourceRelation();
        universityDepartmentRelation.setResource1(university);
        universityDepartmentRelation.setResource2(department);

        ResourceRelation departmentRelation = new ResourceRelation();
        departmentRelation.setResource1(department);
        departmentRelation.setResource2(department);

        assertThat(department.getParents()).containsExactlyInAnyOrder(universityDepartmentRelation, departmentRelation);
        assertThat(department.getChildren()).containsExactly(departmentRelation);

        verify(entityManager, times(1)).flush();
        verify(entityManager, times(1)).refresh(university);
        verify(entityManager, times(1)).refresh(department);

        verify(resourceRelationRepository, times(1)).save(universityDepartmentRelation);
        verify(resourceRelationRepository, times(1)).save(departmentRelation);
    }

}
