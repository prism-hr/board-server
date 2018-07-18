package hr.prism.board.service;

import hr.prism.board.DbTestContext;
import hr.prism.board.domain.Post;
import hr.prism.board.domain.Resource;
import hr.prism.board.domain.ResourceEvent;
import hr.prism.board.domain.User;
import hr.prism.board.enums.Role;
import hr.prism.board.event.EventProducer;
import hr.prism.board.exception.BoardException;
import hr.prism.board.repository.ResourceEventRepository;
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
import java.util.List;

import static hr.prism.board.enums.ResourceEvent.REFERRAL;
import static hr.prism.board.enums.ResourceEvent.VIEW;
import static hr.prism.board.enums.Role.ADMINISTRATOR;
import static hr.prism.board.enums.Role.MEMBER;
import static hr.prism.board.enums.Scope.POST;
import static hr.prism.board.exception.ExceptionCode.UNIDENTIFIABLE_RESOURCE_EVENT;
import static java.time.LocalDateTime.now;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.compare;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.reset;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

@DbTestContext
@RunWith(SpringRunner.class)
public class ResourceEventServiceIT {

    @Inject
    private ResourceEventRepository resourceEventRepository;

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
    @Sql(scripts = {"classpath:data/tearDown.sql", "classpath:data/resourceEventService_GBI_setUp.sql"})
    @Sql(scripts = {"classpath:data/tearDown.sql"}, executionPhase = AFTER_TEST_METHOD)
    public void getById_success() {
        ResourceEvent resourceEvent = resourceEventService.getById(1L);
        assertEquals(1L, resourceEvent.getId().longValue());
    }

    @Test
    @Sql(scripts = {"classpath:data/tearDown.sql", "classpath:data/resourceEventService_PV_SWNR_setUp.sql"})
    @Sql(scripts = {"classpath:data/tearDown.sql"}, executionPhase = AFTER_TEST_METHOD)
    public void processView_successWhenNotRecording() {
        Post post = processViewInSurroundingTransaction(1L, null, null, false);

        assertThat(resourceEventRepository.findAll()).isEmpty();
        verifySummariesEmpty(post);
    }

    @Test
    @Sql(scripts = {"classpath:data/tearDown.sql", "classpath:data/resourceEventService_PV_SWACPAW_setUp.sql"})
    @Sql(scripts = {"classpath:data/tearDown.sql"}, executionPhase = AFTER_TEST_METHOD)
    public void processView_successWhenAdministratorCanPursueApplyWebsite() {
        User user = userService.getById(1L);
        Post post = processViewInSurroundingTransaction(3L, user, null, true);
        Post otherPost = (Post) resourceService.getById(4L);

        List<ResourceEvent> resourceEvents =
            resourceEventRepository.findAll().stream()
                .sorted((event1, event2) -> compare(event1.getId(), event2.getId()))
                .collect(toList());

        assertThat(resourceEvents).hasSize(2);
        verifyResourceEvent(resourceEvents.get(0), post, VIEW, user, ADMINISTRATOR);
        verifyResourceEvent(resourceEvents.get(1), post, REFERRAL, user, ADMINISTRATOR);

        assertNotNull(post.getDemographicDataStatus());
        assertNotNull(post.getReferral());
        assertNull(post.getResponse());

        verifySummariesEmpty(post);
        verifySummariesEmpty(otherPost);
    }

    @Test
    @Sql(scripts = {"classpath:data/tearDown.sql", "classpath:data/resourceEventService_PV_SWACPAE_setUp.sql"})
    @Sql(scripts = {"classpath:data/tearDown.sql"}, executionPhase = AFTER_TEST_METHOD)
    public void processView_successWhenAdministratorCanPursueApplyEmail() {
        User user = userService.getById(1L);
        Post post = processViewInSurroundingTransaction(3L, user, null, true);
        Post otherPost = (Post) resourceService.getById(4L);

        List<ResourceEvent> resourceEvents =
            resourceEventRepository.findAll().stream()
                .sorted((event1, event2) -> compare(event1.getId(), event2.getId()))
                .collect(toList());

        assertThat(resourceEvents).hasSize(1);
        verifyResourceEvent(resourceEvents.get(0), post, VIEW, user, ADMINISTRATOR);

        assertNotNull(post.getDemographicDataStatus());
        assertNull(post.getReferral());
        assertNull(post.getResponse());

        verifySummariesEmpty(post);
        verifySummariesEmpty(otherPost);
    }

    @Test
    @Sql(scripts = {"classpath:data/tearDown.sql", "classpath:data/resourceEventService_PV_SWMCPAW_setUp.sql"})
    @Sql(scripts = {"classpath:data/tearDown.sql"}, executionPhase = AFTER_TEST_METHOD)
    public void processView_successWhenMemberCanPursueApplyWebsite() {
        LocalDateTime runTime = now();
        User user = userService.getById(1L);

        Post post = processViewInSurroundingTransaction(3L, user, null, true);
        Post otherPost = (Post) resourceService.getById(4L);

        List<ResourceEvent> resourceEvents =
            resourceEventRepository.findAll().stream()
                .sorted((event1, event2) -> compare(event1.getId(), event2.getId()))
                .collect(toList());

        assertThat(resourceEvents).hasSize(2);
        verifyResourceEvent(resourceEvents.get(0), post, VIEW, user, MEMBER);
        verifyResourceEvent(resourceEvents.get(1), post, REFERRAL, user, MEMBER);

        assertNotNull(post.getDemographicDataStatus());
        assertNotNull(post.getReferral());
        assertNull(post.getResponse());

        verifySummariesWithPostView(post, runTime);
        verifySummariesEmpty(otherPost);
    }

    @Test
    @Sql(scripts = {"classpath:data/tearDown.sql", "classpath:data/resourceEventService_PV_SWMCPAE_setUp.sql"})
    @Sql(scripts = {"classpath:data/tearDown.sql"}, executionPhase = AFTER_TEST_METHOD)
    public void processView_successWhenMemberCanPursueApplyEmail() {
        LocalDateTime runTime = now();
        User user = userService.getById(1L);

        Post post = processViewInSurroundingTransaction(3L, user, null, true);
        Post otherPost = (Post) resourceService.getById(4L);

        List<ResourceEvent> resourceEvents =
            resourceEventRepository.findAll().stream()
                .sorted((event1, event2) -> compare(event1.getId(), event2.getId()))
                .collect(toList());

        assertThat(resourceEvents).hasSize(1);
        verifyResourceEvent(resourceEvents.get(0), post, VIEW, user, MEMBER);

        assertNotNull(post.getDemographicDataStatus());
        assertNull(post.getReferral());
        assertNull(post.getResponse());

        verifySummariesWithPostView(post, runTime);
        verifySummariesEmpty(otherPost);
    }

    @Test
    @Sql(scripts = {"classpath:data/tearDown.sql", "classpath:data/resourceEventService_PV_FWU_setUp.sql"})
    @Sql(scripts = {"classpath:data/tearDown.sql"}, executionPhase = AFTER_TEST_METHOD)
    public void processView_failureWhenUnidentifiable() {
        assertThatThrownBy(() ->
            processViewInSurroundingTransaction(1L, null, null, true))
            .isExactlyInstanceOf(BoardException.class)
            .hasFieldOrPropertyWithValue("exceptionCode", UNIDENTIFIABLE_RESOURCE_EVENT);
    }

    private Post processViewInSurroundingTransaction(Long postId, User user, String ipAddress, boolean recordView) {
        return new TransactionTemplate(platformTransactionManager).execute(status -> {
            Post post = (Post) resourceService.getResource(user, POST, postId);
            resourceEventService.processView(post, user, ipAddress, recordView);
            return post;
        });
    }

    private void verifyResourceEvent(ResourceEvent resourceEvent, Resource expectedResource,
                                     hr.prism.board.enums.ResourceEvent expectedEvent, User expectedUser,
                                     Role expectedRole) {
        assertEquals(expectedResource, resourceEvent.getResource());
        assertEquals(expectedEvent, resourceEvent.getEvent());
        assertEquals(expectedUser, resourceEvent.getUser());
        assertEquals(expectedRole, resourceEvent.getRole());
    }

    private void verifySummariesEmpty(Post post) {
        assertNull(post.getViewCount());
        assertNull(post.getLastViewTimestamp());
        assertNull(post.getReferralCount());
        assertNull(post.getLastReferralTimestamp());
        assertNull(post.getResponseCount());
        assertNull(post.getLastResponseTimestamp());
    }

    private void verifySummariesWithPostView(Post post, LocalDateTime runTime) {
        assertEquals(1L, post.getViewCount().longValue());
        assertThat(post.getLastViewTimestamp()).isGreaterThanOrEqualTo(runTime);
        assertNull(post.getReferralCount());
        assertNull(post.getLastReferralTimestamp());
        assertNull(post.getResponseCount());
        assertNull(post.getLastResponseTimestamp());
    }

}
