package hr.prism.board.service;

import hr.prism.board.dao.DepartmentDAO;
import hr.prism.board.event.EventProducer;
import hr.prism.board.repository.DepartmentRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.persistence.EntityManager;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(MockitoJUnitRunner.class)
public class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private DepartmentDAO departmentDAO;

    @Mock
    private UserService userService;

    @Mock
    private DocumentService documentService;

    @Mock
    private ResourceService resourceService;

    @Mock
    private DepartmentPatchService resourcePatchService;

    @Mock
    private UserRoleService userRoleService;

    @Mock
    private ActionService actionService;

    @Mock
    private ActivityService activityService;

    @Mock
    private UniversityService universityService;

    @Mock
    private BoardService boardService;

    @Mock
    private ResourceTaskService resourceTaskService;

    @Mock
    private EventProducer eventProducer;

    @Mock
    private EntityManager entityManager;

    private DepartmentService departmentService;

    @Before
    public void setUp() {
        departmentService = new DepartmentService(1L, 1L,
            1L, 1L, departmentRepository,
            departmentDAO, userService, documentService, resourceService, resourcePatchService, userRoleService,
            actionService, activityService, universityService, boardService, resourceTaskService, eventProducer,
            entityManager);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(departmentRepository,
            departmentDAO, userService, documentService, resourceService, resourcePatchService, userRoleService,
            actionService, activityService, universityService, boardService, resourceTaskService, eventProducer,
            entityManager);
        reset(departmentRepository,
            departmentDAO, userService, documentService, resourceService, resourcePatchService, userRoleService,
            actionService, activityService, universityService, boardService, resourceTaskService, eventProducer,
            entityManager);
    }

    @Test
    public void createDepartment_success() {

    }

}
