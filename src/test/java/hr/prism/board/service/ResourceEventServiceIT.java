package hr.prism.board.service;

import hr.prism.board.DbTestContext;
import hr.prism.board.domain.Location;
import hr.prism.board.domain.Post;
import hr.prism.board.domain.ResourceEvent;
import hr.prism.board.domain.User;
import hr.prism.board.dto.ResourceEventDTO;
import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.BoardForbiddenException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.inject.Inject;
import java.time.LocalDateTime;

import static hr.prism.board.enums.AgeRange.THIRTY_THIRTYNINE;
import static hr.prism.board.enums.Gender.MALE;
import static hr.prism.board.enums.MemberCategory.UNDERGRADUATE_STUDENT;
import static hr.prism.board.enums.ResourceEvent.REFERRAL;
import static hr.prism.board.enums.ResourceEvent.VIEW;
import static hr.prism.board.exception.ExceptionCode.*;
import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

@DbTestContext
@RunWith(SpringRunner.class)
@Sql(scripts = {"classpath:data/tearDown.sql", "classpath:data/resourceEvent_setUp.sql"})
@Sql(scripts = {"classpath:data/tearDown.sql"}, executionPhase = AFTER_TEST_METHOD)
public class ResourceEventServiceIT {

    @Inject
    private ResourceEventService resourceEventService;

    @Inject
    private ResourceService resourceService;

    @Inject
    private UserService userService;

    @Inject
    private PlatformTransactionManager platformTransactionManager;

    @Test
    public void createPostView_successWhenUser() {
        new TransactionTemplate(platformTransactionManager).execute(status -> {
            LocalDateTime runTime = now().minusSeconds(1L);
            Post post = (Post) resourceService.getById(3L);
            User user = userService.getById(1L);

            ResourceEvent resourceEvent = resourceEventService.createPostView(post, user, "ipAddress");
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
    public void createPostView_successWhenIpAddress() {
        new TransactionTemplate(platformTransactionManager).execute(status -> {
            LocalDateTime runTime = now().minusSeconds(1L);
            Post post = (Post) resourceService.getById(3L);

            ResourceEvent resourceEvent = resourceEventService.createPostView(post, null, "ipAddress");
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
        assertThatThrownBy(() -> resourceEventService.createPostView(new Post(), null, null))
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

            resourceEventService.getAndConsumeReferral("referral");
            ResourceEvent resourceEvent = resourceEventService.getById(1L);
            Post updatedPost = (Post) resourceService.getById(3L);

            assertEquals(MALE, resourceEvent.getGender());
            assertEquals(THIRTY_THIRTYNINE, resourceEvent.getAgeRange());

            Location expectedLocation = new Location();
            expectedLocation.setGoogleId("googleId");
            assertEquals(expectedLocation, resourceEvent.getLocationNationality());

            assertEquals(UNDERGRADUATE_STUDENT, resourceEvent.getMemberCategory());
            assertEquals("memberProgram", resourceEvent.getMemberProgram());
            assertEquals(2018, resourceEvent.getMemberYear().intValue());

            assertEquals("M400 L535 U536 S335 M516", resourceEvent.getIndexData());
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
        "classpath:data/resourceEvent_setUp.sql", "classpath:data/resourceEvent_setUp_referral.sql"})
    @Sql(scripts = {"classpath:data/tearDown.sql"}, executionPhase = AFTER_TEST_METHOD)
    public void getAndConsumeReferral_failureWhenConsumedOrNotPresent() {
        assertThatThrownBy(() -> resourceEventService.getAndConsumeReferral("consumedOrNotPresent"))
            .isExactlyInstanceOf(BoardForbiddenException.class)
            .hasFieldOrPropertyWithValue("exceptionCode", FORBIDDEN_REFERRAL);
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
    public void createPostResponse_failureWhenAlreadyResponded() {

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

}
