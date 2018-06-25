package hr.prism.board.mapper;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import hr.prism.board.domain.*;
import hr.prism.board.representation.*;
import hr.prism.board.value.DemographicDataStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static hr.prism.board.enums.CategoryType.MEMBER;
import static hr.prism.board.enums.CategoryType.POST;
import static hr.prism.board.enums.ExistingRelation.STUDENT;
import static hr.prism.board.enums.MemberCategory.UNDERGRADUATE_STUDENT;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PostMapperTest {

    @Mock
    private LocationMapper locationMapper;

    @Mock
    private OrganizationMapper organizationMapper;

    @Mock
    private DocumentMapper documentMapper;

    @Mock
    private BoardMapper boardMapper;

    @Mock
    private ResourceMapper resourceMapper;

    @Mock
    private ResourceEventMapper resourceEventMapper;

    private PostMapper postMapper;

    private Board board;

    private Organization organization;

    private Location location;

    private Document applyDocument;

    private LocalDateTime liveTimestamp = LocalDateTime.of(2018, 6, 1, 9, 0, 0);

    private LocalDateTime deadTimestamp = LocalDateTime.of(2018, 6, 2, 9, 0, 0);

    private LocalDateTime lastViewTimestamp = LocalDateTime.of(2018, 6, 1, 10, 0, 0);

    private LocalDateTime lastReferralTimestamp = LocalDateTime.of(2018, 6, 1, 11, 0, 0);

    private LocalDateTime lastResponseTimestamp = LocalDateTime.of(2018, 6, 1, 12, 0, 0);

    private LocalDate expiryDate = LocalDate.of(2020, 6, 1);

    private ResourceEvent resourceEventReferral;

    private ResourceEvent resourceEventResponse;

    private Post post;

    private BoardRepresentation boardRepresentation;

    private OrganizationRepresentation organizationRepresentation;

    private LocationRepresentation locationRepresentation;

    private DocumentRepresentation applyDocumentRepresentation;

    private ResourceEventRepresentation resourceEventReferralRepresentation;

    private ResourceEventRepresentation resourceEventResponseRepresentation;

    @Before
    public void setUp() {
        board = new Board();
        board.setId(1L);

        organization = new Organization();
        organization.setId(1L);

        location = new Location();
        location.setGoogleId("googleId");

        applyDocument = new Document();
        applyDocument.setCloudinaryId("cloudinaryId");

        resourceEventReferral = new ResourceEvent();
        resourceEventReferral.setId(1L);

        resourceEventResponse = new ResourceEvent();
        resourceEventResponse.setId(2L);

        post = new Post();
        post.setId(1L);
        post.setParent(board);
        post.setSummary("summary");
        post.setDescription("description");
        post.setOrganization(organization);
        post.setLocation(location);
        post.setExistingRelation(STUDENT);
        post.setExistingRelationExplanation(
            ImmutableMap.of("explanation", "explanation"));

        post.setCategories(
            ImmutableSet.of(
                new ResourceCategory()
                    .setType(MEMBER)
                    .setName("UNDERGRADUATE_STUDENT"),
                new ResourceCategory()
                    .setType(MEMBER)
                    .setName("MASTER_STUDENT"),
                new ResourceCategory()
                    .setType(POST)
                    .setName("Employment"),
                new ResourceCategory()
                    .setType(POST)
                    .setName("Internship")));

        post.setApplyWebsite("applyWebsite");
        post.setApplyDocument(applyDocument);
        post.setApplyEmail("email@prism.hr");
        post.setLiveTimestamp(liveTimestamp);
        post.setDeadTimestamp(deadTimestamp);
        post.setViewCount(1L);
        post.setReferralCount(2L);
        post.setResponseCount(3L);
        post.setLastViewTimestamp(lastViewTimestamp);
        post.setLastReferralTimestamp(lastReferralTimestamp);
        post.setLastResponseTimestamp(lastResponseTimestamp);

        post.setDemographicDataStatus(
            new DemographicDataStatus()
                .setRequireUserData(true)
                .setRequireMemberData(true)
                .setMemberCategory(UNDERGRADUATE_STUDENT)
                .setMemberProgram("memberProgram")
                .setMemberYear(2018)
                .setExpiryDate(expiryDate));

        post.setReferral(resourceEventReferral);
        post.setResponse(resourceEventResponse);

        boardRepresentation =
            new BoardRepresentation()
                .setId(1L);

        organizationRepresentation =
            new OrganizationRepresentation()
                .setId(1L);

        locationRepresentation =
            new LocationRepresentation()
                .setGoogleId("googleId");

        applyDocumentRepresentation =
            new DocumentRepresentation()
                .setCloudinaryId("cloudinaryId");

        resourceEventReferralRepresentation =
            new ResourceEventRepresentation()
                .setId(1L);

        resourceEventResponseRepresentation =
            new ResourceEventRepresentation()
                .setId(2L);

        when(resourceMapper.apply(post, PostRepresentation.class)).thenReturn(new PostRepresentation());
        when(organizationMapper.apply(organization)).thenReturn(organizationRepresentation);
        when(locationMapper.apply(location)).thenReturn(locationRepresentation);
        when(documentMapper.apply(applyDocument)).thenReturn(applyDocumentRepresentation);
        when(resourceEventMapper.apply(resourceEventReferral)).thenReturn(resourceEventReferralRepresentation);
        when(resourceEventMapper.apply(resourceEventResponse)).thenReturn(resourceEventResponseRepresentation);

        postMapper = new PostMapper(
            locationMapper, organizationMapper, documentMapper, boardMapper, resourceMapper, resourceEventMapper);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(
            locationMapper, organizationMapper, documentMapper, boardMapper, resourceMapper, resourceEventMapper);
        reset(locationMapper, organizationMapper, documentMapper, boardMapper, resourceMapper, resourceEventMapper);
    }

    @Test
    public void apply_success() {

    }

    @Test
    public void apply_successWhenExposeApplyData() {

    }

    @Test
    public void apply_successWhenNull() {
        assertNull(postMapper.apply(null));
    }

    @Test
    public void applySmall_success() {

    }

    @Test
    public void applySmall_successWhenNull() {
        assertNull(postMapper.applySmall(null));
    }

}
