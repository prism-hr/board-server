package hr.prism.board.service;

import hr.prism.board.DbTestContext;
import hr.prism.board.domain.Organization;
import hr.prism.board.dto.OrganizationDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

@DbTestContext
@RunWith(SpringRunner.class)
@Sql("classpath:data/organizationService_setUp.sql")
@Sql(value = "classpath:data/organizationService_tearDown.sql", executionPhase = AFTER_TEST_METHOD)
public class OrganizationServiceIT {

    @Inject
    private OrganizationService organizationService;

    @Inject
    private ServiceHelper serviceHelper;

    @Test
    public void getOrCreateOrganization_successWhenExisting() {
        Organization organization = organizationService.getOrCreateOrganization(
            new OrganizationDTO().setName("organization").setLogo("organization logo"));
        verifyOrganization(organization, "organization", "organization logo");
    }

    @Test
    public void getOrCreateOrganization_successWhenNew() {
        LocalDateTime baseline = LocalDateTime.now();
        OrganizationDTO organizationDTO =
            new OrganizationDTO()
                .setName("new organization")
                .setLogo("new organization logo");

        Organization createdOrganization = organizationService.getOrCreateOrganization(organizationDTO);
        verifyOrganization(createdOrganization, "new organization", "new organization logo");
        serviceHelper.verifyTimestamps(createdOrganization, baseline);

        Organization selectedOrganization = organizationService.getOrCreateOrganization(organizationDTO);
        verifyOrganization(selectedOrganization, "new organization", "new organization logo");
        assertEquals(createdOrganization, selectedOrganization);
    }

    private void verifyOrganization(Organization organization, String expectedName, String expectedLogo) {
        assertNotNull(organization.getId());
        assertEquals(expectedName, organization.getName());
        assertEquals(expectedLogo, organization.getLogo());
    }

}
