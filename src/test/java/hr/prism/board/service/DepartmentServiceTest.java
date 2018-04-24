package hr.prism.board.service;

import hr.prism.board.dao.DepartmentDAO;
import hr.prism.board.domain.Document;
import hr.prism.board.domain.University;
import hr.prism.board.dto.DepartmentDTO;
import hr.prism.board.event.EventProducer;
import hr.prism.board.repository.DepartmentRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.persistence.EntityManager;

import static hr.prism.board.enums.Scope.DEPARTMENT;
import static hr.prism.board.exception.ExceptionCode.DUPLICATE_DEPARTMENT;
import static org.mockito.Mockito.*;

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
    public void createDepartment_successWithDefaults() {
        University university = new University();
        university.setId(1L);

        Document documentLogo = new Document();
        documentLogo.setId(1L);
        university.setDocumentLogo(documentLogo);

        when(universityService.getByIdWithExistenceCheck(1L)).thenReturn(university);

        DepartmentDTO department =
            new DepartmentDTO()
                .setName("department")
                .setSummary("summary");
        departmentService.createDepartment(1L, department);

        verify(universityService, times(1)).getByIdWithExistenceCheck(1L);
        verify(resourceService, times(1)).checkUniqueName(
            DEPARTMENT, null, university, "department", DUPLICATE_DEPARTMENT);
        verify(resourceService, times(1))
            .createHandle(university, DEPARTMENT, "department");
    }

}
