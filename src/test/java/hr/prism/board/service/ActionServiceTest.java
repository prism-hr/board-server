package hr.prism.board.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import hr.prism.board.domain.Resource;
import hr.prism.board.domain.User;
import hr.prism.board.event.EventProducer;
import hr.prism.board.exception.BoardForbiddenException;
import hr.prism.board.representation.ActionRepresentation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;

import static hr.prism.board.enums.Action.EDIT;
import static hr.prism.board.enums.Action.VIEW;
import static hr.prism.board.enums.Scope.DEPARTMENT;
import static hr.prism.board.exception.ExceptionCode.FORBIDDEN_ACTION;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(MockitoJUnitRunner.class)
public class ActionServiceTest {

    @Mock
    private ResourceService resourceService;

    @Mock
    private ActivityService activityService;

    @Mock
    private EventProducer eventProducer;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ApplicationContext applicationContext;

    private ActionService actionService;

    @Before
    public void setUp() {
        actionService = new ActionService(
            resourceService, activityService, eventProducer, objectMapper, applicationContext);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(resourceService, activityService, eventProducer, objectMapper, applicationContext);
        reset(resourceService, activityService, eventProducer, objectMapper, applicationContext);
    }

    @Test
    public void executeAction_failureWhenActionsNull() {
        User user = new User();
        user.setGivenName("alastair");
        user.setSurname("knowles");
        user.setEmail("alastair@prism.hr");

        Resource resource = new Resource();
        resource.setScope(DEPARTMENT);
        resource.setId(1L);

        assertThatThrownBy(() -> actionService.executeAction(user, resource, VIEW, null))
            .isExactlyInstanceOf(BoardForbiddenException.class)
            .hasFieldOrPropertyWithValue("exceptionCode", FORBIDDEN_ACTION);
    }

    @Test
    public void executeAction_failureWhenActionsEmpty() {
        User user = new User();
        user.setGivenName("alastair");
        user.setSurname("knowles");
        user.setEmail("alastair@prism.hr");

        Resource resource = new Resource();
        resource.setScope(DEPARTMENT);
        resource.setId(1L);
        resource.setActions(emptyList());

        assertThatThrownBy(() -> actionService.executeAction(user, resource, VIEW, null))
            .isExactlyInstanceOf(BoardForbiddenException.class)
            .hasFieldOrPropertyWithValue("exceptionCode", FORBIDDEN_ACTION);
    }

    @Test
    public void executeAction_failureWhenActionNotPresent() {
        User user = new User();
        user.setGivenName("alastair");
        user.setSurname("knowles");
        user.setEmail("alastair@prism.hr");

        Resource resource = new Resource();
        resource.setScope(DEPARTMENT);
        resource.setId(1L);
        resource.setActions(ImmutableList.of(new ActionRepresentation().setAction(VIEW)));

        assertThatThrownBy(() -> actionService.executeAction(user, resource, EDIT, null))
            .isExactlyInstanceOf(BoardForbiddenException.class)
            .hasFieldOrPropertyWithValue("exceptionCode", FORBIDDEN_ACTION);
    }

}
