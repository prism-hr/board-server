package hr.prism.board.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import hr.prism.board.domain.Board;
import hr.prism.board.domain.Department;
import hr.prism.board.domain.Resource;
import hr.prism.board.domain.User;
import hr.prism.board.event.EventProducer;
import hr.prism.board.exception.BoardForbiddenException;
import hr.prism.board.exception.BoardNotFoundException;
import hr.prism.board.representation.ActionRepresentation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;

import static hr.prism.board.enums.Action.*;
import static hr.prism.board.enums.Scope.BOARD;
import static hr.prism.board.enums.Scope.DEPARTMENT;
import static hr.prism.board.enums.State.ACCEPTED;
import static hr.prism.board.enums.State.DRAFT;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

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
    public void executeAction_successWhenCreateBoard() {
        User user = new User();
        user.setGivenName("alastair");
        user.setSurname("knowles");
        user.setEmail("alastair@prism.hr");

        Department department = new Department();
        department.setScope(DEPARTMENT);
        department.setId(1L);
        department.setState(DRAFT);
        department.setActions(ImmutableList.of(new ActionRepresentation().setAction(EXTEND).setState(ACCEPTED)));

        Board board = new Board();
        board.setScope(BOARD);
        board.setId(2L);

        when(resourceService.getResource(user, BOARD, 2L)).thenReturn(board);

        actionService.executeAction(user, department, EXTEND, () -> board);

        verify(resourceService, times(1)).updateState(board, ACCEPTED);
        verify(resourceService, times(1)).getResource(user, BOARD, 2L);
        verify(resourceService, times(1)).createResourceOperation(board, EXTEND, user);
    }

    @Test
    public void executeAction_failureWhenResourceNull() {
        assertThatThrownBy(() -> actionService.executeAction(null, null, null, null))
            .isExactlyInstanceOf(BoardNotFoundException.class)
            .hasMessage("RESOURCE_NOT_FOUND: Requested resource does not exist");
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
            .hasMessage("FORBIDDEN_ACTION: Action cannot be performed");
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
            .hasMessage("FORBIDDEN_ACTION: Action cannot be performed");
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
            .hasMessage("FORBIDDEN_ACTION: Action cannot be performed");
    }

}
