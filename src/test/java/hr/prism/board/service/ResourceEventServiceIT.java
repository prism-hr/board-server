package hr.prism.board.service;

import com.google.common.collect.ImmutableMap;
import hr.prism.board.DbTestContext;
import hr.prism.board.domain.*;
import hr.prism.board.dto.DocumentDTO;
import hr.prism.board.dto.ResourceEventDTO;
import hr.prism.board.event.ActivityEvent;
import hr.prism.board.event.EventProducer;
import hr.prism.board.event.NotificationEvent;
import hr.prism.board.exception.BoardDuplicateException;
import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.BoardForbiddenException;
import hr.prism.board.value.DemographicDataStatus;
import hr.prism.board.workflow.Notification;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.inject.Inject;
import java.time.LocalDateTime;

import static hr.prism.board.enums.Activity.RESPOND_POST_ACTIVITY;
import static hr.prism.board.enums.AgeRange.THIRTY_THIRTYNINE;
import static hr.prism.board.enums.Gender.MALE;
import static hr.prism.board.enums.MemberCategory.UNDERGRADUATE_STUDENT;
import static hr.prism.board.enums.Notification.RESPOND_POST_NOTIFICATION;
import static hr.prism.board.enums.ResourceEvent.REFERRAL;
import static hr.prism.board.enums.ResourceEvent.VIEW;
import static hr.prism.board.enums.Role.ADMINISTRATOR;
import static hr.prism.board.enums.Role.MEMBER;
import static hr.prism.board.enums.Scope.POST;
import static hr.prism.board.exception.ExceptionCode.*;
import static java.time.LocalDateTime.now;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

@DbTestContext
@RunWith(SpringRunner.class)
@Sql(scripts = {"classpath:data/tearDown.sql", "classpath:data/resourceEventService_setUp.sql"})
@Sql(scripts = {"classpath:data/tearDown.sql"}, executionPhase = AFTER_TEST_METHOD)
public class ResourceEventServiceIT {

    @Inject
    private ResourceEventService resourceEventService;

    @Inject
    private ResourceService resourceService;

    @Inject
    private PlatformTransactionManager platformTransactionManager;

    @SpyBean
    private UserService userService;

    @MockBean
    private EventProducer eventProducer;

    @After
    public void tearDown() {
        reset(userService, eventProducer);
    }

    @Test
    public void getById_success() {
        ResourceEvent resourceEvent = resourceEventService.getById(1L);
        assertEquals(1L, resourceEvent.getId().longValue());
    }

    @Test
    public void createPostView_successWhenMember() {
        new TransactionTemplate(platformTransactionManager).execute(status -> {
            LocalDateTime runTime = now().minusSeconds(1L);
            Post post = (Post) resourceService.getById(3L);
            User user = userService.getById(1L);

            ResourceEvent resourceEvent = resourceEventService.createPostView(post, user, "ipAddress",
                new DemographicDataStatus().setRole(MEMBER));
            Post updatedPost = (Post) resourceService.getById(3L);

            assertNotNull(resourceEvent.getId());
            assertEquals(post, resourceEvent.getResource());
            assertEquals(user, resourceEvent.getUser());
            assertNull(resourceEvent.getIpAddress());
            assertEquals(VIEW, resourceEvent.getEvent());

            assertEquals(1L, updatedPost.getViewCount().longValue());
            assertThat(updatedPost.getLastViewTimestamp()).isGreaterThan(runTime);
            assertNull(updatedPost.getReferralCount());
            assertNull(updatedPost.getLastReferralTimestamp());
            assertNull(updatedPost.getResponseCount());
            assertNull(updatedPost.getLastResponseTimestamp());

            verifyOtherPost();
            return null;
        });
    }

    @Test
    public void createPostView_successWhenAdministrator() {
        new TransactionTemplate(platformTransactionManager).execute(status -> {
            Post post = (Post) resourceService.getById(3L);
            User user = userService.getById(1L);

            ResourceEvent resourceEvent = resourceEventService.createPostView(post, user, "ipAddress",
                new DemographicDataStatus().setRole(ADMINISTRATOR));
            Post updatedPost = (Post) resourceService.getById(3L);

            assertNull(resourceEvent);

            assertNull(updatedPost.getViewCount());
            assertNull(updatedPost.getLastViewTimestamp());
            assertNull(updatedPost.getReferralCount());
            assertNull(updatedPost.getLastReferralTimestamp());
            assertNull(updatedPost.getResponseCount());
            assertNull(updatedPost.getLastResponseTimestamp());

            verifyOtherPost();
            return null;
        });
    }

    @Test
    public void createPostView_successWhenIpAddress() {
        new TransactionTemplate(platformTransactionManager).execute(status -> {
            LocalDateTime runTime = now().minusSeconds(1L);
            Post post = (Post) resourceService.getById(3L);

            ResourceEvent resourceEvent = resourceEventService.createPostView(post, null, "ipAddress",
                new DemographicDataStatus().setRole(MEMBER));
            Post updatedPost = (Post) resourceService.getById(3L);

            assertNotNull(resourceEvent.getId());
            assertEquals(post, resourceEvent.getResource());
            assertNull(resourceEvent.getUser());
            assertEquals("ipAddress", resourceEvent.getIpAddress());
            assertEquals(VIEW, resourceEvent.getEvent());

            assertEquals(1L, updatedPost.getViewCount().longValue());
            assertThat(updatedPost.getLastViewTimestamp()).isGreaterThan(runTime);
            assertNull(updatedPost.getReferralCount());
            assertNull(updatedPost.getLastReferralTimestamp());
            assertNull(updatedPost.getResponseCount());
            assertNull(updatedPost.getLastResponseTimestamp());

            verifyOtherPost();
            return null;
        });
    }

    @Test
    public void createPostView_failureWhenUserAndIpAddressNull() {
        assertThatThrownBy(() -> resourceEventService.createPostView(
            null, null, null, null))
            .isExactlyInstanceOf(BoardException.class)
            .hasFieldOrPropertyWithValue("exceptionCode", UNIDENTIFIABLE_RESOURCE_EVENT);
    }

    @Test
    public void createPostReferral_success() {
        new TransactionTemplate(platformTransactionManager).execute(status -> {
            Post post = (Post) resourceService.getById(3L);
            User user = userService.getById(1L);

            ResourceEvent resourceEvent = resourceEventService.createPostReferral(post, user);
            Post updatedPost = (Post) resourceService.getById(3L);

            assertNotNull(resourceEvent.getId());
            assertEquals(post, resourceEvent.getResource());
            assertEquals(user, resourceEvent.getUser());
            assertNull(resourceEvent.getIpAddress());
            assertEquals(REFERRAL, resourceEvent.getEvent());
            assertNotNull(resourceEvent.getReferral());

            assertNull(updatedPost.getViewCount());
            assertNull(updatedPost.getLastViewTimestamp());
            assertNull(updatedPost.getReferralCount());
            assertNull(updatedPost.getLastReferralTimestamp());
            assertNull(updatedPost.getResponseCount());
            assertNull(updatedPost.getLastResponseTimestamp());

            verifyOtherPost();
            return null;
        });
    }

    @Test
    public void getAndConsumeReferral_success() {
        new TransactionTemplate(platformTransactionManager).execute(status -> {
            LocalDateTime runTime = now().minusSeconds(1L);
            ResourceEvent resourceEvent = resourceEventService.getById(1L);
            Post updatedPost = (Post) resourceService.getById(3L);

            ResourceEvent referral = resourceEventService.getAndConsumeReferral("referral");
            assertEquals(resourceEvent, referral);

            assertEquals(MALE, resourceEvent.getGender());
            assertEquals(THIRTY_THIRTYNINE, resourceEvent.getAgeRange());

            Location expectedLocation = new Location();
            expectedLocation.setGoogleId("googleId");
            assertEquals(expectedLocation, resourceEvent.getLocationNationality());

            assertEquals(UNDERGRADUATE_STUDENT, resourceEvent.getMemberCategory());
            assertEquals("memberProgram", resourceEvent.getMemberProgram());
            assertEquals(2018, resourceEvent.getMemberYear().intValue());

            assertEquals("M400 030 039 L535 U536 S335 M516 2018", resourceEvent.getIndexData());
            assertNull(resourceEvent.getReferral());

            assertNull(updatedPost.getViewCount());
            assertNull(updatedPost.getLastViewTimestamp());
            assertEquals(1, updatedPost.getReferralCount().longValue());
            assertThat(updatedPost.getLastReferralTimestamp()).isGreaterThan(runTime);
            assertNull(updatedPost.getResponseCount());
            assertNull(updatedPost.getLastResponseTimestamp());
            return null;
        });
    }

    @Test
    @Sql(scripts = {"classpath:data/tearDown.sql",
        "classpath:data/resourceEventService_setUp.sql", "classpath:data/resourceEventService_setUp_referral.sql"})
    @Sql(scripts = {"classpath:data/tearDown.sql"}, executionPhase = AFTER_TEST_METHOD)
    public void getAndConsumeReferral_failureWhenConsumedOrNotPresent() {
        assertThatThrownBy(() -> resourceEventService.getAndConsumeReferral("consumedOrNotPresent"))
            .isExactlyInstanceOf(BoardForbiddenException.class)
            .hasFieldOrPropertyWithValue("exceptionCode", FORBIDDEN_REFERRAL);
    }

    @Test
    public void createPostResponse_success() {
        new TransactionTemplate(platformTransactionManager).execute(status -> {
            User user = userService.getById(1L);
            verifyCreatePostResponse(user, false);

            verify(userService, times(0))
                .updateUserResume(eq(user), any(Document.class), any(String.class));
            return null;
        });
    }

    @Test
    public void createPostResponse_successWhenDefaultResume() {
        new TransactionTemplate(platformTransactionManager).execute(status -> {
            User user = userService.getById(1L);
            verifyCreatePostResponse(user, true);

            Document documentResume = new Document();
            documentResume.setCloudinaryId("cloudinaryId");

            verify(userService, times(1))
                .updateUserResume(user, documentResume, "websiteResume");
            return null;
        });
    }

    @Test
    public void createPostResponse_failureWhenNoApplyEmail() {
        Post post = (Post) resourceService.getById(4L);
        User user = userService.getById(1L);

        assertThatThrownBy(() -> resourceEventService.createPostResponse(post, user, new ResourceEventDTO()))
            .isExactlyInstanceOf(BoardException.class)
            .hasFieldOrPropertyWithValue("exceptionCode", INVALID_RESOURCE_EVENT);
    }

    @Test
    @Sql(scripts = {"classpath:data/tearDown.sql",
        "classpath:data/resourceEventService_setUp.sql", "classpath:data/resourceEventService_setUp_response.sql"})
    @Sql(scripts = {"classpath:data/tearDown.sql"}, executionPhase = AFTER_TEST_METHOD)
    public void createPostResponse_failureWhenAlreadyResponded() {
        Post post = (Post) resourceService.getById(3L);
        User user = userService.getById(2L);

        assertThatThrownBy(() -> resourceEventService.createPostResponse(post, user, new ResourceEventDTO()))
            .isExactlyInstanceOf(BoardDuplicateException.class)
            .hasFieldOrPropertyWithValue("exceptionCode", DUPLICATE_RESOURCE_EVENT)
            .hasFieldOrPropertyWithValue("properties", ImmutableMap.of("id", 3L));
    }

    @Test
    public void getResourceEvent_success() {
        Post post = (Post) resourceService.getById(3L);
        User user = userService.getById(1L);

        ResourceEvent resourceEvent = resourceEventService.getResourceEvent(post, REFERRAL, user);
        assertEquals(1L, resourceEvent.getId().longValue());
    }

    @Test
    public void getResourceEvent_successWhenEmpty() {
        Post post = (Post) resourceService.getById(4L);
        User user = userService.getById(1L);

        assertNull(resourceEventService.getResourceEvent(post, REFERRAL, user));
    }

    @Test
    public void getResourceEvents_success() {
        Post post = (Post) resourceService.getById(3L);
        User user = userService.getById(1L);
        ResourceEvent resourceEvent = resourceEventService.getById(1L);

        assertThat(resourceEventService
            .getResourceEvents(singletonList(post), REFERRAL, user)).containsExactly(resourceEvent);
    }

    private void verifyOtherPost() {
        Post otherPost = (Post) resourceService.getById(4L);
        assertNull(otherPost.getViewCount());
        assertNull(otherPost.getLiveTimestamp());
        assertNull(otherPost.getReferralCount());
        assertNull(otherPost.getLastReferralTimestamp());
        assertNull(otherPost.getResponseCount());
        assertNull(otherPost.getLastResponseTimestamp());
    }

    private void verifyCreatePostResponse(User user, Boolean defaultResume) {
        LocalDateTime runTime = now().minusSeconds(1L);
        Post post = (Post) resourceService.getById(3L);

        ResourceEvent resourceEvent = resourceEventService.createPostResponse(post, user,
            new ResourceEventDTO()
                .setDocumentResume(
                    new DocumentDTO()
                        .setCloudinaryId("cloudinaryId")
                        .setCloudinaryUrl("cloudinaryUrl")
                        .setFileName("fileName"))
                .setWebsiteResume("websiteResume")
                .setCoveringNote("coveringNote")
                .setDefaultResume(defaultResume));

        Post updatedPost = (Post) resourceService.getById(3L);

        assertEquals(MALE, resourceEvent.getGender());
        assertEquals(THIRTY_THIRTYNINE, resourceEvent.getAgeRange());

        Location expectedLocation = new Location();
        expectedLocation.setGoogleId("googleId");
        assertEquals(expectedLocation, resourceEvent.getLocationNationality());

        assertEquals(UNDERGRADUATE_STUDENT, resourceEvent.getMemberCategory());
        assertEquals("memberProgram", resourceEvent.getMemberProgram());
        assertEquals(2018, resourceEvent.getMemberYear().intValue());

        assertEquals("M400 030 039 L535 U536 S335 M516 2018", resourceEvent.getIndexData());

        assertNull(updatedPost.getViewCount());
        assertNull(updatedPost.getLastViewTimestamp());
        assertNull(updatedPost.getReferralCount());
        assertNull(updatedPost.getLastReferralTimestamp());
        assertEquals(1, updatedPost.getResponseCount().longValue());
        assertThat(updatedPost.getLastResponseTimestamp()).isGreaterThan(runTime);

        Long responseId = resourceEvent.getId();
        verify(eventProducer, times(1)).produce(
            new ActivityEvent(this, 3L, ResourceEvent.class, responseId,
                singletonList(
                    new hr.prism.board.workflow.Activity()
                        .setScope(POST)
                        .setRole(ADMINISTRATOR)
                        .setActivity(RESPOND_POST_ACTIVITY))),
            new NotificationEvent(this, 3L, responseId,
                singletonList(
                    new Notification()
                        .setNotification(RESPOND_POST_NOTIFICATION)
                        .addAttachment(
                            new Notification.Attachment()
                                .setName("fileName")
                                .setUrl("cloudinaryUrl")
                                .setLabel("Application")))));
    }

}
