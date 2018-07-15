package hr.prism.board.service;

import hr.prism.board.DbTestContext;
import hr.prism.board.domain.ResourceEvent;
import hr.prism.board.event.EventProducer;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.reset;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

@DbTestContext
@RunWith(SpringRunner.class)
@Sql(scripts = {"classpath:data/tearDown.sql", "classpath:data/resourceEventService_setUp.sql"})
@Sql(scripts = {"classpath:data/tearDown.sql"}, executionPhase = AFTER_TEST_METHOD)
public class ResourceEventServiceIT {

    @Inject
    private ResourceEventService resourceEventService;

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

}
