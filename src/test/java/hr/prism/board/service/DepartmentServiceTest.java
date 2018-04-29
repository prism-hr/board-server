package hr.prism.board.service;

import com.google.common.collect.ImmutableList;
import hr.prism.board.dao.DepartmentDAO;
import hr.prism.board.domain.Department;
import hr.prism.board.domain.Document;
import hr.prism.board.domain.University;
import hr.prism.board.domain.User;
import hr.prism.board.dto.BoardDTO;
import hr.prism.board.dto.DepartmentDTO;
import hr.prism.board.dto.DocumentDTO;
import hr.prism.board.event.EventProducer;
import hr.prism.board.repository.DepartmentRepository;
import hr.prism.board.value.ResourceFilter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

import static hr.prism.board.enums.Action.EXTEND;
import static hr.prism.board.enums.CategoryType.MEMBER;
import static hr.prism.board.enums.MemberCategory.*;
import static hr.prism.board.enums.ResourceTask.DEPARTMENT_TASKS;
import static hr.prism.board.enums.Role.ADMINISTRATOR;
import static hr.prism.board.enums.Scope.DEPARTMENT;
import static hr.prism.board.enums.State.DRAFT;
import static hr.prism.board.exception.ExceptionCode.DUPLICATE_DEPARTMENT;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
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
    public void createDepartment_successWithDefaultData() {
        LocalDateTime baseline = LocalDateTime.now().minusSeconds(1);

        User user = new User();
        user.setId(1L);

        when(userService.getUserSecured()).thenReturn(user);

        University university = new University();
        university.setId(1L);

        Document documentLogo = new Document();
        documentLogo.setId(1L);
        university.setDocumentLogo(documentLogo);

        when(universityService.getById(1L)).thenReturn(university);

        when(resourceService.createHandle(university, DEPARTMENT, "department"))
            .thenReturn("university/department");

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

        Department department = departmentCaptor.getValue();
        assertEquals("department", department.getName());
        assertEquals("summary", department.getSummary());
        assertEquals(documentLogo, department.getDocumentLogo());
        assertEquals("university/department", department.getHandle());
        assertThat(department.getLastTaskCreationTimestamp()).isGreaterThan(baseline);

        verify(userService, times(1)).getUserSecured();
        verify(universityService, times(1)).getById(1L);
        verify(resourceService, times(1)).checkUniqueName(
            DEPARTMENT, null, university, "department", DUPLICATE_DEPARTMENT);

        verify(resourceService, times(1))
            .createHandle(university, DEPARTMENT, "department");
        verify(departmentRepository, times(1)).save(any(Department.class));
        verify(resourceService, times(1)).updateState(department, DRAFT);

        verify(resourceService, times(1))
            .updateCategories(department, MEMBER, MEMBER_CATEGORY_STRINGS);
        verify(resourceService, times(1)).createResourceRelation(university, department);
        verify(resourceService, times(1)).setIndexDataAndQuarter(department);
        verify(resourceService, times(1)).createResourceOperation(department, EXTEND, user);
        verify(userRoleService, times(1)).createUserRole(department, user, ADMINISTRATOR);

        verify(boardService, times(1))
            .createBoard(eq(2L), argThat(boardNameMatcher("Career Opportunities")));
        verify(boardService, times(1))
            .createBoard(eq(2L), argThat(boardNameMatcher("Research Opportunities")));

        verify(resourceTaskService, times(1))
            .createForNewResource(2L, 1L, DEPARTMENT_TASKS);

        verify(entityManager, times(1)).refresh(department);
        verify(resourceService, times(1)).getResource(user, DEPARTMENT, 2L);
    }

    @Test
    public void createDepartment_successWithCustomData() {
        LocalDateTime baseline = LocalDateTime.now().minusSeconds(1);

        User user = new User();
        user.setId(1L);

        when(userService.getUserSecured()).thenReturn(user);

        University university = new University();
        university.setId(1L);

        when(universityService.getById(1L)).thenReturn(university);

        when(resourceService.createHandle(university, DEPARTMENT, "department"))
            .thenReturn("university/department");

        ArgumentCaptor<Department> departmentCaptor = ArgumentCaptor.forClass(Department.class);
        when(departmentRepository.save(departmentCaptor.capture())).thenAnswer(invocation -> {
            Department department = (Department) invocation.getArguments()[0];
            department.setId(2L);
            return department;
        });

        DocumentDTO documentDTO =
            new DocumentDTO()
                .setCloudinaryId("cloudinaryId")
                .setCloudinaryUrl("cloudinaryUrl")
                .setFileName("fileName");

        departmentService.createDepartment(1L,
            new DepartmentDTO()
                .setName("department")
                .setSummary("summary")
                .setDocumentLogo(
                    new DocumentDTO()
                        .setCloudinaryId("cloudinaryId")
                        .setCloudinaryUrl("cloudinaryUrl")
                        .setFileName("fileName"))
                .setMemberCategories(ImmutableList.of(MASTER_STUDENT, RESEARCH_STUDENT)));

        Department department = departmentCaptor.getValue();
        assertEquals("department", department.getName());
        assertEquals("summary", department.getSummary());
        assertEquals("university/department", department.getHandle());
        assertThat(department.getLastTaskCreationTimestamp()).isGreaterThan(baseline);

        verify(userService, times(1)).getUserSecured();
        verify(universityService, times(1)).getById(1L);
        verify(resourceService, times(1)).checkUniqueName(
            DEPARTMENT, null, university, "department", DUPLICATE_DEPARTMENT);
        verify(documentService, times(1)).getOrCreateDocument(documentDTO);

        verify(resourceService, times(1))
            .createHandle(university, DEPARTMENT, "department");
        verify(departmentRepository, times(1)).save(any(Department.class));
        verify(resourceService, times(1)).updateState(department, DRAFT);

        verify(resourceService, times(1))
            .updateCategories(department, MEMBER, ImmutableList.of("MASTER_STUDENT", "RESEARCH_STUDENT"));
        verify(resourceService, times(1)).createResourceRelation(university, department);
        verify(resourceService, times(1)).setIndexDataAndQuarter(department);
        verify(resourceService, times(1)).createResourceOperation(department, EXTEND, user);
        verify(userRoleService, times(1)).createUserRole(department, user, ADMINISTRATOR);

        verify(boardService, times(1))
            .createBoard(eq(2L), argThat(boardNameMatcher("Career Opportunities")));
        verify(boardService, times(1))
            .createBoard(eq(2L), argThat(boardNameMatcher("Research Opportunities")));

        verify(resourceTaskService, times(1))
            .createForNewResource(2L, 1L, DEPARTMENT_TASKS);

        verify(entityManager, times(1)).refresh(department);
        verify(resourceService, times(1)).getResource(user, DEPARTMENT, 2L);
    }

    @Test
    public void getDepartments_success() {
        User user = new User();
        user.setId(1L);

        ResourceFilter filter =
            new ResourceFilter()
                .setScope(DEPARTMENT)
                .setSearchTerm("department")
                .setOrderStatement("resource.name");

        Department department = new Department();
        department.setId(1L);

        when(userService.getUser()).thenReturn(user);
        when(resourceService.getResources(user, filter)).thenReturn(singletonList(department));

        List<Department> departments = departmentService.getDepartments(
            new ResourceFilter()
                .setSearchTerm("department"));

        assertThat(departments).containsExactly(department);

        verify(userService, times(1)).getUser();
        verify(resourceService, times(1)).getResources(user, filter);
    }

    @Test
    public void getDepartments_successWhenUnauthenticated() {
        ResourceFilter filter =
            new ResourceFilter()
                .setScope(DEPARTMENT)
                .setSearchTerm("department")
                .setOrderStatement("resource.name");

        Department department = new Department();
        department.setId(1L);

        when(resourceService.getResources(null, filter)).thenReturn(singletonList(department));

        List<Department> departments = departmentService.getDepartments(
            new ResourceFilter()
                .setSearchTerm("department"));

        assertThat(departments).containsExactly(department);

        verify(userService, times(1)).getUser();
        verify(resourceService, times(1)).getResources(null, filter);
    }

    private static ArgumentMatcher<BoardDTO> boardNameMatcher(String name) {
        return new ArgumentMatcher<BoardDTO>() {

            @Override
            public boolean matches(Object argument) {
                return name.equals(((BoardDTO) argument).getName());
            }

        };
    }

}
