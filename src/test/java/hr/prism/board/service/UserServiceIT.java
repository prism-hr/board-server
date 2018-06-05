package hr.prism.board.service;

import hr.prism.board.DbTestContext;
import hr.prism.board.domain.Location;
import hr.prism.board.domain.Organization;
import hr.prism.board.domain.User;
import hr.prism.board.repository.LocationRepository;
import hr.prism.board.repository.OrganizationRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

@DbTestContext
@RunWith(SpringRunner.class)
@Sql(scripts = {"classpath:data/tearDown.sql", "classpath:data/userService_setUp.sql"})
@Sql(scripts = {"classpath:data/tearDown.sql"}, executionPhase = AFTER_TEST_METHOD)
public class UserServiceIT {

    @Inject
    private OrganizationRepository organizationRepository;

    @Inject
    private LocationRepository locationRepository;

    @Inject
    private UserService userService;

    @Test
    public void updateUserOrganizationAndLocation_success() {
        User user = userService.getById(1L);
        Organization organization = organizationRepository.findOne(1L);
        Location location = locationRepository.findOne(1L);

        userService.updateUserOrganizationAndLocation(user, organization, location);
        User updatedUser = userService.getById(1L);

        assertEquals(organization, updatedUser.getDefaultOrganization());
        assertEquals(location, updatedUser.getDefaultLocation());
    }

}
