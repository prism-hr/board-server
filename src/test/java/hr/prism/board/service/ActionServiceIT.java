package hr.prism.board.service;

import com.google.common.collect.ImmutableList;
import hr.prism.board.ApplicationConfiguration;
import hr.prism.board.api.AbstractIT;
import hr.prism.board.api.BoardApi;
import hr.prism.board.api.PostApi;
import hr.prism.board.domain.ResourceAction;
import hr.prism.board.dto.*;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.RelationWithDepartment;
import hr.prism.board.enums.State;
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
    private UserTestService userTestService;


    @Test
    public void shouldDepartmentUserBeAbleToAcceptPost() {
        BoardRepresentation sampleBoard = postSampleBoard("department@poczta.fm");
        PostRepresentation samplePost = postSamplePost(sampleBoard.getId(), "poster@poczta.fm");
        verifySubmittedPost(samplePost);

        transactionTemplate.execute(status -> {
            postApi.acceptPost(samplePost.getId(), new PostPatchDTO().setDescription(Optional.of("Corrected desc")));
            return null;
        });

        transactionTemplate.execute(status -> {
            userTestService.authenticateAs("poster@poczta.fm");
            PostRepresentation postR = postApi.getPost(samplePost.getId());
            assertThat(postR.getActions(), Matchers.containsInAnyOrder(new ResourceAction(Action.VIEW), new ResourceAction(Action.EDIT), new ResourceAction(Action.WITHDRAW, State.WITHDRAWN)));
            assertEquals(State.ACCEPTED, postR.getState());
            assertEquals("Corrected desc", postR.getDescription());
            return null;
        });
    }

    @Test
    public void shouldDepartmentUserBeAbleToRejectPost() {
        BoardRepresentation sampleBoard = postSampleBoard("department@poczta.fm");
        PostRepresentation samplePost = postSamplePost(sampleBoard.getId(), "poster@poczta.fm");
        verifySubmittedPost(samplePost);

        transactionTemplate.execute(status -> {
            postApi.rejectPost(samplePost.getId(), new PostPatchDTO());
            return null;
        });

        transactionTemplate.execute(transactionStatus -> {
            userTestService.authenticateAs("poster@poczta.fm");
            PostRepresentation postR = postApi.getPost(samplePost.getId());
            assertThat(postR.getActions(), Matchers.containsInAnyOrder(new ResourceAction(Action.EDIT), new ResourceAction(Action.VIEW), new ResourceAction(Action.WITHDRAW, State.WITHDRAWN)));
            assertEquals(State.REJECTED, postR.getState());
            return null;
        });
    }

    @Test
    public void shouldPosterBeAbleToCorrectPost() {
        BoardRepresentation sampleBoard = postSampleBoard("department@poczta.fm");
        PostRepresentation samplePost = postSamplePost(sampleBoard.getId(), "poster@poczta.fm");
        verifySubmittedPost(samplePost);

        transactionTemplate.execute(status -> {
            postApi.suspendPost(samplePost.getId(), new PostPatchDTO());
            return null;
        });

        transactionTemplate.execute(status -> {
            userTestService.authenticateAs("poster@poczta.fm");
            PostRepresentation postR = postApi.getPost(samplePost.getId());
            assertThat(postR.getActions(), Matchers.containsInAnyOrder(new ResourceAction(Action.EDIT), new ResourceAction(Action.VIEW), new ResourceAction(Action.CORRECT, State.DRAFT), new ResourceAction(Action.WITHDRAW, State.WITHDRAWN)));
            assertEquals(State.SUSPENDED, postR.getState());
            return null;
        });

        transactionTemplate.execute(status -> {
            postApi.correctPost(samplePost.getId(), new PostPatchDTO().setName(Optional.of("Corrected name")));
            return null;
        });

        transactionTemplate.execute(status -> {
            userTestService.authenticateAs("department@poczta.fm");
            PostRepresentation postR = postApi.getPost(samplePost.getId());
            assertThat(postR.getActions(), Matchers.containsInAnyOrder(new ResourceAction(Action.VIEW), new ResourceAction(Action.EDIT), new ResourceAction(Action.ACCEPT, State.ACCEPTED), new ResourceAction(Action.REJECT, State.REJECTED), new ResourceAction(Action.SUSPEND, State.SUSPENDED)));
            assertEquals(State.DRAFT, postR.getState());
            assertEquals("Corrected name", postR.getName());
            return null;
        });
    }

    private void verifySubmittedPost(PostRepresentation samplePost) {
        transactionTemplate.execute(status -> {
            userTestService.authenticateAs("department@poczta.fm");
            PostRepresentation postR = postApi.getPost(samplePost.getId());
            assertThat(postR.getActions(), Matchers.containsInAnyOrder(new ResourceAction(Action.VIEW), new ResourceAction(Action.EDIT), new ResourceAction(Action.ACCEPT, State.ACCEPTED), new ResourceAction(Action.REJECT, State.REJECTED), new ResourceAction(Action.SUSPEND, State.SUSPENDED)));
            return null;
        });
    }

    private BoardRepresentation postSampleBoard(String user) {
        userTestService.authenticateAs(user);

        return transactionTemplate.execute(status -> {
            BoardDTO boardDTO = new BoardDTO()
                .setName("ActionServiceIT Board")
                .setPurpose("Purpose")
                .setPostCategories(ImmutableList.of("p1", "p2", "p3"))
                .setDepartment(new DepartmentDTO()
                    .setName("ActionServiceIT Department")
                    .setMemberCategories(ImmutableList.of("m1", "m2", "m3")));
            return boardApi.postBoard(boardDTO);
        });
    }

    private PostRepresentation postSamplePost(Long boardId, String user) {
        userTestService.authenticateAs(user);
        return transactionTemplate.execute(status -> {
            BoardRepresentation boardR = boardApi.getBoard(boardId);
            return postApi.postPost(boardR.getId(),
                new PostDTO()
                    .setName("Department Post")
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
                    .setExistingRelation(RelationWithDepartment.FAMILY));
        });
    }

}
