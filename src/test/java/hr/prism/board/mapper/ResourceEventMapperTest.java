package hr.prism.board.mapper;

import hr.prism.board.domain.Document;
import hr.prism.board.domain.Location;
import hr.prism.board.domain.ResourceEvent;
import hr.prism.board.domain.User;
import hr.prism.board.representation.DocumentRepresentation;
import hr.prism.board.representation.LocationRepresentation;
import hr.prism.board.representation.ResourceEventRepresentation;
import hr.prism.board.representation.UserRepresentation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDateTime;

import static hr.prism.board.enums.AgeRange.NINETEEN_TWENTYFOUR;
import static hr.prism.board.enums.Gender.FEMALE;
import static hr.prism.board.enums.MemberCategory.UNDERGRADUATE_STUDENT;
import static hr.prism.board.enums.ResourceEvent.RESPONSE;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ResourceEventMapperTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private DocumentMapper documentMapper;

    @Mock
    private LocationMapper locationMapper;

    private ResourceEventMapper resourceEventMapper;

    private Location locationNationality;

    private User user;

    private Document documentResume;

    private ResourceEvent resourceEvent;

    private LocationRepresentation locationNationalityRepresentation;

    private UserRepresentation userRepresentation;

    private DocumentRepresentation documentResumeRepresentation;

    private LocalDateTime createdTimestamp = LocalDateTime.of(2018, 6, 1, 9, 0, 0);

    @Before
    public void setUp() {
        locationNationality = new Location();
        locationNationality.setGoogleId("locationNationality");

        user = new User();
        user.setId(1L);

        documentResume = new Document();
        documentResume.setCloudinaryId("documentResume");

        resourceEvent = new ResourceEvent();
        resourceEvent.setId(1L);
        resourceEvent.setEvent(RESPONSE);
        resourceEvent.setReferral("referral");
        resourceEvent.setViewed(true);
        resourceEvent.setGender(FEMALE);
        resourceEvent.setAgeRange(NINETEEN_TWENTYFOUR);
        resourceEvent.setLocationNationality(locationNationality);
        resourceEvent.setMemberCategory(UNDERGRADUATE_STUDENT);
        resourceEvent.setMemberProgram("memberProgram");
        resourceEvent.setMemberYear(2018);
        resourceEvent.setUser(user);
        resourceEvent.setIpAddress("ipAddress");
        resourceEvent.setDocumentResume(documentResume);
        resourceEvent.setWebsiteResume("websiteResume");
        resourceEvent.setCoveringNote("coveringNote");
        resourceEvent.setCreatedTimestamp(createdTimestamp);

        locationNationalityRepresentation =
            new LocationRepresentation()
                .setGoogleId("locationNationality");

        userRepresentation =
            new UserRepresentation()
                .setId(1L);

        documentResumeRepresentation =
            new DocumentRepresentation()
                .setCloudinaryId("documentResume");

        when(locationMapper.apply(locationNationality)).thenReturn(locationNationalityRepresentation);
        when(userMapper.apply(user)).thenReturn(userRepresentation);
        when(documentMapper.apply(documentResume)).thenReturn(documentResumeRepresentation);

        resourceEventMapper = new ResourceEventMapper(userMapper, documentMapper, locationMapper);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(userMapper, documentMapper, locationMapper);
        reset(userMapper, documentMapper, locationMapper);
    }

    @Test
    public void apply_success() {
        ResourceEventRepresentation resourceEventRepresentation = resourceEventMapper.apply(resourceEvent);
        verifyResourceEventRepresentation(resourceEventRepresentation, null,
            null, null, null, null);

        verify(locationMapper, times(1)).apply(locationNationality);
    }

    @Test
    public void apply_successWhenExposeResponseData() {
        resourceEvent.setExposeResponseData(true);
        ResourceEventRepresentation resourceEventRepresentation = resourceEventMapper.apply(resourceEvent);
        verifyResourceEventRepresentation(resourceEventRepresentation, userRepresentation, "ipAddress",
            documentResumeRepresentation, "websiteResume", "coveringNote");

        verify(locationMapper, times(1)).apply(locationNationality);
        verify(userMapper, times(1)).apply(user);
        verify(documentMapper, times(1)).apply(documentResume);
    }

    @Test
    public void apply_successWhenNull() {
        assertNull(resourceEventMapper.apply(null));
    }

    private void verifyResourceEventRepresentation(ResourceEventRepresentation resourceEventRepresentation,
                                                   UserRepresentation expectedUser, String expectedIpAddress,
                                                   DocumentRepresentation documentResumeExpected,
                                                   String websiteResumeExpected, String coveringNoteExpected) {
        assertEquals(1L, resourceEventRepresentation.getId().longValue());
        assertEquals(RESPONSE, resourceEventRepresentation.getEvent());
        assertEquals("referral", resourceEventRepresentation.getReferral());
        assertTrue(resourceEventRepresentation.isViewed());
        assertEquals(FEMALE, resourceEventRepresentation.getGender());
        assertEquals(NINETEEN_TWENTYFOUR, resourceEventRepresentation.getAgeRange());
        assertEquals(locationNationalityRepresentation, resourceEventRepresentation.getLocationNationality());
        assertEquals(UNDERGRADUATE_STUDENT, resourceEventRepresentation.getMemberCategory());
        assertEquals("memberProgram", resourceEventRepresentation.getMemberProgram());
        assertEquals(2018, resourceEventRepresentation.getMemberYear().intValue());
        assertEquals(expectedUser, resourceEventRepresentation.getUser());
        assertEquals(expectedIpAddress, resourceEventRepresentation.getIpAddress());
        assertEquals(documentResumeExpected, resourceEventRepresentation.getDocumentResume());
        assertEquals(websiteResumeExpected, resourceEventRepresentation.getWebsiteResume());
        assertEquals(coveringNoteExpected, resourceEventRepresentation.getCoveringNote());
        assertEquals(createdTimestamp, resourceEventRepresentation.getCreatedTimestamp());
    }

}
