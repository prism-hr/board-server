package hr.prism.board.mapper;

import hr.prism.board.domain.Organization;
import hr.prism.board.representation.OrganizationRepresentation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(MockitoJUnitRunner.class)
public class OrganizationMapperTest {

    private OrganizationMapper organizationMapper = new OrganizationMapper();

    @Test
    public void apply_success() {
        Organization organization = new Organization();
        organization.setId(1L);
        organization.setName("name");
        organization.setLogo("logo");

        OrganizationRepresentation organizationRepresentation = organizationMapper.apply(organization);
        assertEquals(1, organizationRepresentation.getId().longValue());
        assertEquals("name", organizationRepresentation.getName());
        assertEquals("logo", organizationRepresentation.getLogo());
    }

    @Test
    public void apply_successWhenNull() {
        assertNull(organizationMapper.apply((Organization) null));
    }

}
