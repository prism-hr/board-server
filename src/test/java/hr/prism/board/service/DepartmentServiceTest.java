package hr.prism.board.service;

import hr.prism.board.dao.DepartmentDAO;
import hr.prism.board.domain.Department;
import hr.prism.board.domain.Document;
import hr.prism.board.domain.University;
import hr.prism.board.domain.User;
import hr.prism.board.dto.DepartmentDTO;
import hr.prism.board.event.EventProducer;
import hr.prism.board.repository.DepartmentRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.persistence.EntityManager;

import static hr.prism.board.enums.CategoryType.MEMBER;
import static hr.prism.board.enums.MemberCategory.MEMBER_CATEGORY_STRINGS;
import static hr.prism.board.enums.Scope.DEPARTMENT;
import static hr.prism.board.enums.State.DRAFT;
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
        when(userService.getUserSecured()).thenReturn(new User());

        University university = new University();
        university.setId(1L);

        Document documentLogo = new Document();
        documentLogo.setId(1L);
        university.setDocumentLogo(documentLogo);

        when(universityService.getByIdWithExistenceCheck(1L)).thenReturn(university);

        ArgumentCaptor<Department> departmentCaptor = ArgumentCaptor.forClass(Department.class);
        when(departmentRepository.save(departmentCaptor.capture())).thenAnswer(invocation -> {
            Department department = (Department) invocation.getArguments()[0];
            department.setId(2L);
            return department;
        });

        departmentService.createDepartment(1L,
            new DepartmentDTO()
                .setName("department")
                .setSummary("summary"));

        verify(userService, times(1)).getUserSecured();
        verify(universityService, times(1)).getByIdWithExistenceCheck(1L);
        verify(resourceService, times(1)).checkUniqueName(
            DEPARTMENT, null, university, "department", DUPLICATE_DEPARTMENT);
        verify(resourceService, times(1))
            .createHandle(university, DEPARTMENT, "department");
        verify(departmentRepository, times(1)).save(any(Department.class));

        Department department = departmentCaptor.getValue();
        verify(resourceService, times(1)).updateState(department, DRAFT);
        verify(resourceService, times(1))
            .updateCategories(department, MEMBER, MEMBER_CATEGORY_STRINGS);
        verify(resourceService, times(1)).createResourceRelation(university, department);
    }

}
