package hr.prism.board.service;

import hr.prism.board.DbTestContext;
import hr.prism.board.domain.Post;
import hr.prism.board.domain.ResourceEvent;
import hr.prism.board.domain.User;
import hr.prism.board.exception.BoardException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.inject.Inject;
import java.time.LocalDateTime;

import static hr.prism.board.enums.ResourceEvent.REFERRAL;
import static hr.prism.board.enums.ResourceEvent.VIEW;
import static hr.prism.board.exception.ExceptionCode.UNIDENTIFIABLE_RESOURCE_EVENT;
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
            Post post = (Post) resourceService.getById(1L);
            User user = userService.getById(1L);

            ResourceEvent resourceEvent = resourceEventService.createPostView(post, user, "ipAddress");
            Post updatedPost = (Post) resourceService.getById(1L);
            Post otherPost = (Post) resourceService.getById(2L);

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

            verifyOther(otherPost);
            return null;
        });
    }

    @Test
    public void createPostView_successWhenIpAddress() {
        new TransactionTemplate(platformTransactionManager).execute(status -> {
            LocalDateTime runTime = now().minusSeconds(1L);
            Post post = (Post) resourceService.getById(1L);

            ResourceEvent resourceEvent = resourceEventService.createPostView(post, null, "ipAddress");
            Post updatedPost = (Post) resourceService.getById(1L);
            Post otherPost = (Post) resourceService.getById(2L);

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

            verifyOther(otherPost);
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
            Post post = (Post) resourceService.getById(1L);
            User user = userService.getById(1L);

            ResourceEvent resourceEvent = resourceEventService.createPostReferral(post, user);
            Post updatedPost = (Post) resourceService.getById(1L);
            Post otherPost = (Post) resourceService.getById(2L);

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

            verifyOther(otherPost);
            return null;
        });
    }

    @Test
    public void getAndConsumeReferral_success() {

    }

    @Test
    @Sql(scripts = {"classpath:data/tearDown.sql",
        "classpath:data/resourceEvent_setUp.sql", "classpath:data/resourceEvent_referral_setUp.sql"})
    @Sql(scripts = {"classpath:data/tearDown.sql"}, executionPhase = AFTER_TEST_METHOD)
    public void getAndConsumeReferral_failureWhenConsumedOrNotPresent() {

    }

    private void verifyOther(Post otherPost) {
        assertNull(otherPost.getViewCount());
        assertNull(otherPost.getLiveTimestamp());
        assertNull(otherPost.getReferralCount());
        assertNull(otherPost.getLastReferralTimestamp());
        assertNull(otherPost.getResponseCount());
        assertNull(otherPost.getLastResponseTimestamp());
    }

}
