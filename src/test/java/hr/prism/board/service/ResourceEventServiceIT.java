package hr.prism.board.service;

import hr.prism.board.DbTestContext;
import hr.prism.board.domain.Post;
import hr.prism.board.domain.Resource;
import hr.prism.board.domain.ResourceEvent;
import hr.prism.board.domain.User;
import hr.prism.board.enums.Role;
import hr.prism.board.event.EventProducer;
import hr.prism.board.repository.ResourceEventRepository;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.util.List;

import static hr.prism.board.enums.ResourceEvent.REFERRAL;
import static hr.prism.board.enums.ResourceEvent.VIEW;
import static hr.prism.board.enums.Role.ADMINISTRATOR;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
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
        Post post = resourceEventService.processView(1L, null, "ip-address", false);

        assertThat(resourceEventRepository.findAll()).isEmpty();
        verifySummariesEmpty(post);
    }

    @Test
    @Sql(scripts = {"classpath:data/tearDown.sql", "classpath:data/resourceEventService_PV_SWACPAW_setUp.sql"})
    @Sql(scripts = {"classpath:data/tearDown.sql"}, executionPhase = AFTER_TEST_METHOD)
    public void processView_successWhenAdministratorCanPursueApplyWebsite() {
        User user = userService.getById(1L);
        Post post = resourceEventService.processView(3L, user, null, true);
        Post otherPost = (Post) resourceService.getById(4L);

        List<ResourceEvent> resourceEvents =
            resourceEventRepository.findAll().stream()
                .sorted((event1, event2) -> ObjectUtils.compare(event1.getId(), event2.getId()))
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

}
