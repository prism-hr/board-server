package hr.prism.board.service;

import hr.prism.board.dao.UserRoleDAO;
import hr.prism.board.domain.Department;
import hr.prism.board.domain.User;
import hr.prism.board.domain.UserRole;
import hr.prism.board.repository.UserRoleRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.persistence.EntityManager;

import static hr.prism.board.enums.Role.ADMINISTRATOR;
import static hr.prism.board.enums.State.ACCEPTED;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UserRoleServiceTest {

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private UserRoleDAO userRoleDAO;

    @Mock
    private EntityManager entityManager;

    private UserRoleService userRoleService;

    @Before
    public void setUp() {
        userRoleService = new UserRoleService(userRoleRepository, userRoleDAO, entityManager);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(userRoleRepository, userRoleDAO, entityManager);
        reset(userRoleRepository, userRoleDAO, entityManager);
    }

    @Test
    public void createUserRole_successWhenDepartmentAdministrator() {
        Department department = new Department();
        department.setId(1L);
        department.setCreatorId(1L);

        User user = new User();
        user.setId(2L);

        when(userRoleRepository.save(any(UserRole.class))).thenAnswer(invocation -> invocation.getArguments()[0]);

        UserRole userRole = userRoleService.createUserRole(department, user, ADMINISTRATOR);
        assertNotNull(userRole.getUuid());
        assertEquals(department, userRole.getResource());
        assertEquals(user, userRole.getUser());
        assertEquals(ADMINISTRATOR, userRole.getRole());
        assertEquals(ACCEPTED, userRole.getState());
        assertTrue(userRole.isCreated());

        verify(userRoleRepository, times(1))
            .save(argThat(
                new ArgumentMatcher<UserRole>() {

                    @Override
                    public boolean matches(Object argument) {
                        UserRole userRole = (UserRole) argument;
                        return department.equals(userRole.getResource())
                            && user.equals(userRole.getUser())
                            && ADMINISTRATOR == userRole.getRole()
                            && ACCEPTED == userRole.getState();
                    }

                }));
    }

}
