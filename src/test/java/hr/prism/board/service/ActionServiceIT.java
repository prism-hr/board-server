package hr.prism.board.service;

import com.google.common.collect.ImmutableList;
import hr.prism.board.ApplicationConfiguration;
import hr.prism.board.api.AbstractIT;
import hr.prism.board.api.BoardApi;
import hr.prism.board.api.PostApi;
import hr.prism.board.domain.Post;
import hr.prism.board.domain.User;
import hr.prism.board.dto.*;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.RelationWithDepartment;
import hr.prism.board.enums.State;
import hr.prism.board.exception.ExceptionUtil;
import hr.prism.board.representation.BoardRepresentation;
import hr.prism.board.representation.PostRepresentation;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {ApplicationConfiguration.class})
@TestPropertySource(value = {"classpath:application.properties", "classpath:test.properties"})
public class ActionServiceIT extends AbstractIT {
    
    @Inject
    private PostApi postApi;
    
    @Inject
    private BoardApi boardApi;
    
    @Inject
    private ActionService actionService;
    
    @Inject
    private PostService postService;
    
    @Inject
    private UserTestService userTestService;
    
    @Test
    public void shouldDepartmentUserBeAbleToAcceptPost() {
        BoardRepresentation board = postBoard("department@poczta.fm");
        PostRepresentation post = postPost(board.getId(), "poster@poczta.fm");
    
        Long postId = post.getId();
        verifyPost(postId, "department@poczta.fm", State.DRAFT,
            Arrays.asList(Action.VIEW, Action.EDIT, Action.ACCEPT, Action.REJECT, Action.SUSPEND),
            Arrays.asList(Action.WITHDRAW));
        verifyPost(postId, "poster@poczta.fm", State.DRAFT,
            Arrays.asList(Action.VIEW, Action.EDIT, Action.WITHDRAW),
            Arrays.asList(Action.ACCEPT, Action.REJECT, Action.SUSPEND));
    
        userTestService.authenticateAs("department@poczta.fm");
        transactionTemplate.execute(status -> {
            postApi.acceptPost(postId, new PostPatchDTO().setDescription(Optional.of("Corrected desc")));
            return null;
        });
    
        verifyPost(postId, "department@poczta.fm", State.ACCEPTED,
            Arrays.asList(Action.VIEW, Action.EDIT, Action.REJECT, Action.SUSPEND),
            Arrays.asList(Action.WITHDRAW));
        verifyPost(postId, "poster@poczta.fm", State.ACCEPTED,
            Arrays.asList(Action.VIEW, Action.EDIT, Action.WITHDRAW),
            Arrays.asList(Action.REJECT, Action.SUSPEND));
        
        transactionTemplate.execute(status -> {
            PostRepresentation postR = postApi.getPost(postId);
            assertEquals("Corrected desc", postR.getDescription());
            return null;
        });
    }
    
    @Test
    public void shouldPosterBeAbleToCorrectPost() {
        BoardRepresentation board = postBoard("department@poczta.fm");
        PostRepresentation post = postPost(board.getId(), "poster@poczta.fm");
    
        Long postId = post.getId();
        verifyPost(postId, "department@poczta.fm", State.DRAFT,
            Arrays.asList(Action.VIEW, Action.EDIT, Action.ACCEPT, Action.REJECT, Action.SUSPEND),
            Arrays.asList(Action.WITHDRAW));
        verifyPost(postId, "poster@poczta.fm", State.DRAFT,
            Arrays.asList(Action.VIEW, Action.EDIT, Action.WITHDRAW),
            Arrays.asList(Action.ACCEPT, Action.REJECT, Action.SUSPEND));
    
        userTestService.authenticateAs("department@poczta.fm");
        transactionTemplate.execute(status -> {
            postApi.suspendPost(post.getId(), new PostPatchDTO());
            return null;
        });
    
        verifyPost(postId, "department@poczta.fm", State.SUSPENDED,
            Arrays.asList(Action.VIEW, Action.EDIT, Action.ACCEPT, Action.REJECT),
            Arrays.asList(Action.CORRECT, Action.WITHDRAW));
        verifyPost(postId, "poster@poczta.fm", State.SUSPENDED,
            Arrays.asList(Action.VIEW, Action.EDIT, Action.CORRECT, Action.WITHDRAW),
            Arrays.asList(Action.ACCEPT, Action.REJECT));
    
        userTestService.authenticateAs("poster@poczta.fm");
        transactionTemplate.execute(status -> {
            postApi.correctPost(postId, new PostPatchDTO().setName(Optional.of("Corrected name")));
            return null;
        });
    
        verifyPost(postId, "department@poczta.fm", State.DRAFT,
            Arrays.asList(Action.VIEW, Action.EDIT, Action.ACCEPT, Action.REJECT, Action.SUSPEND),
            Arrays.asList(Action.WITHDRAW));
        verifyPost(postId, "poster@poczta.fm", State.DRAFT,
            Arrays.asList(Action.VIEW, Action.EDIT, Action.WITHDRAW),
            Arrays.asList(Action.ACCEPT, Action.REJECT, Action.SUSPEND));
        
        transactionTemplate.execute(status -> {
            PostRepresentation postR = postApi.getPost(postId);
            assertEquals("Corrected name", postR.getName());
            return null;
        });
    }
    
    @Test
    public void shouldDepartmentUserBeAbleToRejectAndRestorePost() {
        BoardRepresentation board = postBoard("department@poczta.fm");
        PostRepresentation post = postPost(board.getId(), "poster@poczta.fm");
        
        Long postId = post.getId();
        verifyPost(postId, "department@poczta.fm", State.DRAFT,
            Arrays.asList(Action.VIEW, Action.EDIT, Action.ACCEPT, Action.REJECT, Action.SUSPEND),
            Arrays.asList(Action.WITHDRAW));
        verifyPost(postId, "poster@poczta.fm", State.DRAFT,
            Arrays.asList(Action.VIEW, Action.EDIT, Action.WITHDRAW),
            Arrays.asList(Action.ACCEPT, Action.REJECT, Action.SUSPEND));
        
        userTestService.authenticateAs("department@poczta.fm");
        transactionTemplate.execute(status -> {
            postApi.rejectPost(postId, new PostPatchDTO());
            return null;
        });
        
        verifyPost(postId, "department@poczta.fm", State.REJECTED,
            Arrays.asList(Action.VIEW, Action.EDIT, Action.ACCEPT, Action.SUSPEND, Action.RESTORE),
            Arrays.asList(Action.WITHDRAW));
        verifyPost(postId, "poster@poczta.fm", State.REJECTED,
            Arrays.asList(Action.VIEW, Action.EDIT, Action.WITHDRAW),
            Arrays.asList(Action.ACCEPT, Action.SUSPEND, Action.RESTORE));
        
        userTestService.authenticateAs("department@poczta.fm");
        transactionTemplate.execute(status -> {
            postApi.restorePost(postId, new PostPatchDTO());
            return null;
        });
        
        verifyPost(postId, "department@poczta.fm", State.DRAFT,
            Arrays.asList(Action.VIEW, Action.EDIT, Action.ACCEPT, Action.REJECT, Action.SUSPEND),
            Arrays.asList(Action.WITHDRAW));
        verifyPost(postId, "poster@poczta.fm", State.DRAFT,
            Arrays.asList(Action.VIEW, Action.EDIT, Action.WITHDRAW),
            Arrays.asList(Action.ACCEPT, Action.REJECT, Action.SUSPEND));
    }
    
    @Test
    public void shouldPosterBeAbleToWithdrawAndRestorePost() {
        BoardRepresentation board = postBoard("department@poczta.fm");
        PostRepresentation post = postPost(board.getId(), "poster@poczta.fm");
        
        Long postId = post.getId();
        verifyPost(postId, "department@poczta.fm", State.DRAFT,
            Arrays.asList(Action.VIEW, Action.EDIT, Action.ACCEPT, Action.REJECT, Action.SUSPEND),
            Arrays.asList(Action.WITHDRAW));
        verifyPost(postId, "poster@poczta.fm", State.DRAFT,
            Arrays.asList(Action.VIEW, Action.EDIT, Action.WITHDRAW),
            Arrays.asList(Action.ACCEPT, Action.REJECT, Action.SUSPEND));
        
        userTestService.authenticateAs("poster@poczta.fm");
        transactionTemplate.execute(status -> {
            postApi.withdrawPost(postId, new PostPatchDTO());
            return null;
        });
        
        verifyPost(postId, "department@poczta.fm", State.WITHDRAWN,
            Arrays.asList(Action.VIEW, Action.EDIT),
            Arrays.asList(Action.RESTORE));
        verifyPost(postId, "poster@poczta.fm", State.WITHDRAWN,
            Arrays.asList(Action.VIEW, Action.EDIT, Action.RESTORE),
            Collections.emptyList());
        
        userTestService.authenticateAs("poster@poczta.fm");
        transactionTemplate.execute(status -> {
            postApi.restorePost(postId, new PostPatchDTO());
            return null;
        });
        
        verifyPost(postId, "department@poczta.fm", State.DRAFT,
            Arrays.asList(Action.VIEW, Action.EDIT, Action.ACCEPT, Action.REJECT, Action.SUSPEND),
            Arrays.asList(Action.WITHDRAW));
        verifyPost(postId, "poster@poczta.fm", State.DRAFT,
            Arrays.asList(Action.VIEW, Action.EDIT, Action.WITHDRAW),
            Arrays.asList(Action.ACCEPT, Action.REJECT, Action.SUSPEND));
    }
    
    private void verifyPost(Long postId, String username, State state, Collection<Action> actions, Collection<Action> forbiddenActions) {
        User user = userTestService.authenticateAs(username);
        transactionTemplate.execute(status -> {
            PostRepresentation postR = postApi.getPost(postId);
            assertEquals(state, postR.getState());
            assertThat(postR.getActions(), Matchers.containsInAnyOrder(actions));
            return null;
        });
        
        Post post = postService.getPost(postId);
        for (Action forbiddenAction : forbiddenActions) {
            ExceptionUtil.verifyApiForbiddenException(() -> actionService.executeAction(user, post, forbiddenAction, null),
                user.toString() + " cannot " + forbiddenAction.name().toLowerCase() + " " + post.toString());
        }
    }
    
    private BoardRepresentation postBoard(String user) {
        userTestService.authenticateAs(user);
        
        return transactionTemplate.execute(status -> {
            BoardDTO boardDTO = new BoardDTO()
                .setName("Board")
                .setPurpose("Purpose")
                .setPostCategories(ImmutableList.of("p1", "p2", "p3"))
                .setDepartment(new DepartmentDTO()
                    .setName("Department")
                    .setMemberCategories(ImmutableList.of("m1", "m2", "m3")));
            return boardApi.postBoard(boardDTO);
        });
    }
    
    private PostRepresentation postPost(Long boardId, String user) {
        userTestService.authenticateAs(user);
        return transactionTemplate.execute(status -> {
            BoardRepresentation boardR = boardApi.getBoard(boardId);
            return postApi.postPost(boardR.getId(),
                new PostDTO()
                    .setName("Post")
                    .setDescription("desc")
                    .setOrganizationName("org")
                    .setLocation(new LocationDTO()
                        .setName("BB")
                        .setDomicile("PL")
                        .setGoogleId("sss")
                        .setLatitude(BigDecimal.ONE)
                        .setLongitude(BigDecimal.ONE))
                    .setPostCategories(Collections.emptyList())
                    .setMemberCategories(Collections.emptyList())
                    .setExistingRelation(RelationWithDepartment.STUDENT));
        });
    }
    
}
