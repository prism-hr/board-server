package hr.prism.board.mapper;

import hr.prism.board.domain.Document;
import hr.prism.board.domain.Location;
import hr.prism.board.domain.Organization;
import hr.prism.board.domain.User;
import hr.prism.board.representation.DocumentRepresentation;
import hr.prism.board.representation.LocationRepresentation;
import hr.prism.board.representation.OrganizationRepresentation;
import hr.prism.board.value.UserSearch;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UserMapperTest {

    @Mock
    private DocumentMapper documentMapper;

    @Mock
    private OrganizationMapper organizationMapper;

    @Mock
    private LocationMapper locationMapper;

    private UserMapper userMapper;

    private Document documentImage;

    private Location locationNationality;

    private Document documentResume;

    private Organization defaultOrganization;

    private Location defaultLocation;

    private DocumentRepresentation documentImageRepresentation;

    private LocationRepresentation locationNationalityRepresentation;

    private DocumentRepresentation documentResumeRepresentation;

    private OrganizationRepresentation defaultOrganizationRepresentation;

    private LocationRepresentation defaultLocationRepresentation;

    @Before
    public void setUp() {
        documentImage = new Document();
        documentImage.setCloudinaryId("documentImage");

        locationNationality = new Location();
        locationNationality.setGoogleId("nationality");

        documentResume = new Document();
        documentResume.setCloudinaryId("documentResume");

        defaultOrganization = new Organization();
        defaultOrganization.setName("defaultOrganization");

        defaultLocation = new Location();
        defaultLocation.setGoogleId("defaultLocation");

        documentImageRepresentation =
            new DocumentRepresentation()
                .setCloudinaryId("documentImage");

        locationNationalityRepresentation =
            new LocationRepresentation()
                .setGoogleId("nationality");

        documentResumeRepresentation =
            new DocumentRepresentation()
                .setCloudinaryId("documentResume");

        defaultOrganizationRepresentation =
            new OrganizationRepresentation()
                .setName("defaultOrganization");

        defaultLocationRepresentation =
            new LocationRepresentation()
                .setGoogleId("defaultLocation");

        when(documentMapper.apply(documentImage)).thenReturn(documentImageRepresentation);
        when(locationMapper.apply(locationNationality)).thenReturn(locationNationalityRepresentation);
        when(documentMapper.apply(documentResume)).thenReturn(documentResumeRepresentation);
        when(organizationMapper.apply(defaultOrganization)).thenReturn(defaultOrganizationRepresentation);
        when(locationMapper.apply(defaultLocation)).thenReturn(defaultLocationRepresentation);

        userMapper = new UserMapper(documentMapper, organizationMapper, locationMapper);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(documentMapper, organizationMapper, locationMapper);
        reset(documentMapper, organizationMapper, locationMapper);
    }

    @Test
    public void apply_success() {

    }

    @Test
    public void apply_successWhenNull() {
        assertNull(userMapper.apply((User) null));
    }

    @Test
    public void apply_userSearch_success() {

    }

    @Test
    public void apply_userSearch_successWhenNull() {
        assertNull(userMapper.apply((UserSearch) null));
    }

}
