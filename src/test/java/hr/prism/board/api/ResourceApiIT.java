package hr.prism.board.api;

import com.google.common.collect.ImmutableSet;
import hr.prism.board.TestContext;
import hr.prism.board.TestHelper;
import hr.prism.board.domain.Role;
import hr.prism.board.domain.Scope;
import hr.prism.board.domain.User;
import hr.prism.board.dto.BoardDTO;
import hr.prism.board.dto.ResourceUserDTO;
import hr.prism.board.dto.UserDTO;
import hr.prism.board.representation.BoardRepresentation;
import hr.prism.board.representation.ResourceUserRepresentation;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.util.List;

import static org.hamcrest.Matchers.*;

@SuppressWarnings("unchecked")
@TestContext
@RunWith(SpringRunner.class)
public class ResourceApiIT extends AbstractIT {

    @Inject
    private ResourceApi resourceApi;

    @Inject
    private BoardApi boardApi;

    @Test
    public void shouldAddAndRemoveRoles() {
        User currentUser = testUserService.authenticate();
        BoardDTO boardDTO = TestHelper.sampleBoard();

        BoardRepresentation boardR = boardApi.postBoard(boardDTO);

        addAndRemoveUserRoles(currentUser, Scope.BOARD, boardR.getId());
        addAndRemoveUserRoles(currentUser, Scope.DEPARTMENT, boardR.getDepartment().getId());
    }

    private void addAndRemoveUserRoles(User currentUser, Scope scope, Long resourceId) {
        List<ResourceUserRepresentation> resourceUsers = resourceApi.getResourceUsers(scope, resourceId);
        Assert.assertThat(resourceUsers, contains(resourceUserMatcher(currentUser.getEmail(), Role.ADMINISTRATOR)));

        // add resource user with two roles
        UserDTO newUser = new UserDTO().setEmail("board-manager@mail.com").setGivenName("Sample").setSurname("User");
        ResourceUserRepresentation boardManager = resourceApi.addResourceUser(scope, resourceId,
            new ResourceUserDTO().setUser(newUser).setRoles(ImmutableSet.of(Role.MEMBER, Role.AUTHOR)));
        resourceUsers = resourceApi.getResourceUsers(scope, resourceId);
        Assert.assertThat(resourceUsers, containsInAnyOrder(resourceUserMatcher(currentUser.getEmail(), Role.ADMINISTRATOR),
            resourceUserMatcher("board-manager@mail.com", Role.MEMBER, Role.AUTHOR)));

        // remove one role
        resourceApi.removeUserRole(scope, resourceId, boardManager.getUser().getId(), Role.AUTHOR);
        resourceUsers = resourceApi.getResourceUsers(scope, resourceId);
        Assert.assertThat(resourceUsers, containsInAnyOrder(resourceUserMatcher(currentUser.getEmail(), Role.ADMINISTRATOR),
            resourceUserMatcher("board-manager@mail.com", Role.MEMBER)));

        // add one role
        resourceApi.addUserRole(scope, resourceId, boardManager.getUser().getId(), Role.ADMINISTRATOR);
        resourceUsers = resourceApi.getResourceUsers(scope, resourceId);
        Assert.assertThat(resourceUsers, containsInAnyOrder(resourceUserMatcher(currentUser.getEmail(), Role.ADMINISTRATOR),
            resourceUserMatcher("board-manager@mail.com", Role.MEMBER, Role.ADMINISTRATOR)));

        // remove user from resource
        resourceApi.removeResourceUser(scope, resourceId, boardManager.getUser().getId());
        resourceUsers = resourceApi.getResourceUsers(scope, resourceId);
        Assert.assertThat(resourceUsers, contains(resourceUserMatcher(currentUser.getEmail(), Role.ADMINISTRATOR)));
    }

    public Matcher<ResourceUserRepresentation> resourceUserMatcher(String email, Role... roles) {
        Matcher<Object> emailMatcher = hasProperty("user", hasProperty("email", equalTo(email)));
        return allOf(emailMatcher, hasProperty("roles", containsInAnyOrder(roles)));
    }

}
