package hr.prism.board.interceptor;

import hr.prism.board.domain.*;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.State;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PostStateChangeInterceptorTest {

    private PostStateChangeInterceptor subject = new PostStateChangeInterceptor();

    @Test
    public void shouldAmendNewStateFromAcceptedToPendingForRejectedDepartment() {
        University university = new University();
        university.setState(State.ACCEPTED);

        Department department = new Department();
        department.setParent(university);
        department.setState(State.REJECTED);

        Board board = new Board();
        board.setParent(department);
        board.setState(State.ACCEPTED);

        Post post = new Post();
        post.setParent(board);
        post.setState(State.ACCEPTED);

        State newState = subject.intercept(new User(), post, Action.ACCEPT, State.ACCEPTED);
        Assert.assertEquals(State.PENDING, newState);
    }

}
