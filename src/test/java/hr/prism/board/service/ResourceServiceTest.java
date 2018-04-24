package hr.prism.board.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import hr.prism.board.dao.ResourceDAO;
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
    }

    @Test
    public void getById() {
        resourceService.getById(1L);
        verify(resourceRepository, times(1)).findOne(1L);
    }

}
