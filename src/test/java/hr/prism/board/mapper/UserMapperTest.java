package hr.prism.board.mapper;

import hr.prism.board.domain.Document;
import hr.prism.board.domain.Location;
import hr.prism.board.domain.Organization;
import hr.prism.board.domain.User;
import hr.prism.board.representation.DocumentRepresentation;
import hr.prism.board.representation.LocationRepresentation;
import hr.prism.board.representation.OrganizationRepresentation;
import hr.prism.board.representation.UserRepresentation;
import hr.prism.board.value.UserSearch;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static hr.prism.board.enums.AgeRange.TWENTYFIVE_TWENTYNINE;
import static hr.prism.board.enums.DocumentRequestState.DISPLAY_NEVER;
import static hr.prism.board.enums.Gender.MALE;
import static org.junit.Assert.*;
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

    private User user;

    private UserSearch userSearch;

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
        locationNationality.setGoogleId("locationNationality");

        documentResume = new Document();
        documentResume.setCloudinaryId("documentResume");

        defaultOrganization = new Organization();
        defaultOrganization.setName("defaultOrganization");

        defaultLocation = new Location();
        defaultLocation.setGoogleId("defaultLocation");

        user = new User();
        user.setId(1L);
        user.setGivenName("givenName");
        user.setSurname("surname");
        user.setEmail("email@prism.com");
        user.setDocumentImage(documentImage);
        user.setDocumentImageRequestState(DISPLAY_NEVER);
        user.setSeenWalkThrough(true);
        user.setGender(MALE);
        user.setAgeRange(TWENTYFIVE_TWENTYNINE);
        user.setLocationNationality(locationNationality);
        user.setDocumentResume(documentResume);
        user.setWebsiteResume("websiteResume");
        user.setDepartmentAdministrator(true);
        user.setPostCreator(true);
        user.setDefaultOrganization(defaultOrganization);
        user.setDefaultLocation(defaultLocation);
        user.setPassword("password");

        userSearch = new UserSearch(1L, "givenName", "surname", "e...l@prism.hr",
            "cloudinaryId", "cloudinaryUrl", "fileName");

        documentImageRepresentation =
            new DocumentRepresentation()
                .setCloudinaryId("documentImage");

        locationNationalityRepresentation =
            new LocationRepresentation()
                .setGoogleId("locationNationality");

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
        when(documentMapper.apply(userSearch)).thenReturn(documentImageRepresentation);

        userMapper = new UserMapper(documentMapper, organizationMapper, locationMapper);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(documentMapper, organizationMapper, locationMapper);
        reset(documentMapper, organizationMapper, locationMapper);
    }

    @Test
    public void apply_success() {
        UserRepresentation userRepresentation = userMapper.apply(user);
        verifyUserRepresentation(userRepresentation, "e...l@prism.com");
    }

    @Test
    public void apply_successWhenRevealEmail() {
        user.setRevealEmail(true);
        UserRepresentation userRepresentation = userMapper.apply(user);
        verifyUserRepresentation(userRepresentation, "email@prism.com");
    }

    @Test
    public void apply_successWhenNull() {
        assertNull(userMapper.apply((User) null));
    }

    @Test
    public void apply_userSearch_success() {
        UserRepresentation userRepresentation = userMapper.apply(userSearch);

        assertEquals(1L, userRepresentation.getId().longValue());
        assertEquals("givenName", userRepresentation.getGivenName());
        assertEquals("surname", userRepresentation.getSurname());
        assertEquals("e...l@prism.hr", userRepresentation.getEmail());
        assertEquals(documentImageRepresentation, userRepresentation.getDocumentImage());

        verify(documentMapper, times(1)).apply(userSearch);
    }

    @Test
    public void apply_userSearch_successWhenNull() {
        assertNull(userMapper.apply((UserSearch) null));
    }

    private void verifyUserRepresentation(UserRepresentation userRepresentation, String expectedEmailAddress) {
        assertEquals(1L, userRepresentation.getId().longValue());
        assertEquals("givenName", userRepresentation.getGivenName());
        assertEquals("surname", userRepresentation.getSurname());
        assertEquals(expectedEmailAddress, userRepresentation.getEmail());
        assertEquals(documentImageRepresentation, userRepresentation.getDocumentImage());
        assertEquals(DISPLAY_NEVER, userRepresentation.getDocumentImageRequestState());
        assertTrue(userRepresentation.getSeenWalkThrough());
        assertEquals(MALE, userRepresentation.getGender());
        assertEquals(TWENTYFIVE_TWENTYNINE, userRepresentation.getAgeRange());
        assertEquals(locationNationalityRepresentation, userRepresentation.getLocationNationality());
        assertEquals(documentResumeRepresentation, userRepresentation.getDocumentResume());
        assertEquals("websiteResume", userRepresentation.getWebsiteResume());
        assertTrue(userRepresentation.isDepartmentAdministrator());
        assertTrue(userRepresentation.isPostCreator());
        assertEquals(defaultOrganizationRepresentation, userRepresentation.getDefaultOrganization());
        assertEquals(defaultLocationRepresentation, userRepresentation.getDefaultLocation());

        verify(documentMapper, times(1)).apply(documentImage);
        verify(locationMapper, times(1)).apply(locationNationality);
        verify(documentMapper, times(1)).apply(documentResume);
        verify(organizationMapper, times(1)).apply(defaultOrganization);
        verify(locationMapper, times(1)).apply(defaultLocation);
    }

}
