package hr.prism.board.service;

import hr.prism.board.authentication.AuthenticationToken;
import hr.prism.board.dao.UserDAO;
import hr.prism.board.domain.User;
import hr.prism.board.repository.UserRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static io.jsonwebtoken.lang.Assert.isNull;
import static io.jsonwebtoken.lang.Assert.notNull;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.security.core.context.SecurityContextHolder.getContext;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserDAO userDAO;

    @Mock
    private UserPatchService userPatchService;

    @Mock
    private LocationService locationService;

    private UserService userService;

    @Before
    public void setUp() {
        userService = new UserService(
            1L, userRepository, userDAO, userPatchService, locationService);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(userRepository, userDAO, userPatchService, locationService);
        reset(userRepository, userDAO, userPatchService, locationService);
        getContext().setAuthentication(null);
    }

    @Test
    public void getUser_success() {
        getContext().setAuthentication(
            new AuthenticationToken(
                new User()));

        notNull(userService.getUser());
    }

    @Test
    public void getUser_successWhenNotAuthenticated() {
        isNull(userService.getUser());
    }

}
