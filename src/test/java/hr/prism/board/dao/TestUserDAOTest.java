package hr.prism.board.dao;

import com.google.common.collect.ImmutableList;
import hr.prism.board.DbTestContext;
import hr.prism.board.domain.Organization;
import hr.prism.board.repository.OrganizationRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

@DbTestContext
@RunWith(SpringRunner.class)
@Sql(scripts = {"classpath:data/tearDown.sql", "classpath:data/testUserDAO_setUp.sql"})
@Sql(scripts = {"classpath:data/tearDown.sql"}, executionPhase = AFTER_TEST_METHOD)
public class TestUserDAOTest {

    @Inject
    private OrganizationRepository organizationRepository;

    @Inject
    private TestUserDAO testUserDAO;

    @Test
    public void getTablesWithCreatorIdColumn_success() {
        List<String> tablesNames = testUserDAO.getTablesWithCreatorIdColumn();

        assertThat(tablesNames).containsExactly(
            "activity",
            "activity_event",
            "activity_role",
            "activity_user",
            "document",
            "location",
            "organization",
            "resource",
            "resource_category",
            "resource_event",
            "resource_event_search",
            "resource_operation",
            "resource_relation",
            "resource_search",
            "resource_task",
            "test_email",
            "user",
            "user_notification_suppression",
            "user_role",
            "user_search");
    }

    @Test
    public void deleteRecords_success() {
        testUserDAO.deleteRecords("organization", ImmutableList.of(2L));
        List<Organization> activities = organizationRepository.findAll();

        assertThat(activities).containsExactly(
            new Organization().setName("organization1"));
    }

}
