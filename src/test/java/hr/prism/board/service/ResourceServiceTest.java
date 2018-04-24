package hr.prism.board.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import hr.prism.board.dao.ResourceDAO;
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

import static hr.prism.board.enums.Scope.DEPARTMENT;
import static hr.prism.board.exception.ExceptionCode.DUPLICATE_DEPARTMENT;
import static java.util.Collections.emptyList;
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

}
