package hr.prism.board.service;

import com.google.common.collect.ImmutableList;
import hr.prism.board.dao.TestUserDAO;
import hr.prism.board.repository.UserRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TestUserServiceTest {

    @Mock
    private TestUserDAO testUserDAO;

    @Mock
    private UserRepository userRepository;

    private TestUserService testUserService;

    @Before
    public void setUp() {
        testUserService = new TestUserService(userRepository, testUserDAO);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(testUserDAO, userRepository);
        reset(testUserDAO, userRepository);
    }

    @Test
    public void deleteTestUsers_success() {
        when(testUserDAO.getTablesWithCreatorIdColumn())
            .thenReturn(ImmutableList.of("location", "organization"));

        when(userRepository.findByTestUser(true))
            .thenReturn(ImmutableList.of(1L));

        testUserService.deleteTestUsers();

        List<Long> users = ImmutableList.of(1L);

        verify(userRepository, times(1)).findByTestUser(true);
        verify(testUserDAO, times(1)).disableForeignKeyChecks();
        verify(testUserDAO, times(1)).getTablesWithCreatorIdColumn();
        verify(testUserDAO, times(1)).deleteRecords("location", users);
        verify(testUserDAO, times(1)).deleteRecords("organization", users);
        verify(testUserDAO, times(1)).enableForeignKeyChecks();
    }

    @Test
    public void deleteTestUsers_successWhenNoUsers() {
        testUserService.deleteTestUsers();

        verify(userRepository, times(1)).findByTestUser(true);
    }

}
