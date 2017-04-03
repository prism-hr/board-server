package hr.prism.board.api;

import com.google.common.collect.ImmutableList;
import hr.prism.board.ApplicationConfiguration;
import hr.prism.board.dto.*;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.RelationWithDepartment;
import hr.prism.board.enums.State;
import hr.prism.board.representation.BoardRepresentation;
import hr.prism.board.representation.PostRepresentation;
import hr.prism.board.service.UserTestService;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {ApplicationConfiguration.class})
@TestPropertySource(value = {"classpath:application.properties", "classpath:test.properties"})
public class ActionsIT extends AbstractIT {

    @Inject
    private PostApi postApi;

    @Inject
    private DepartmentBoardApi departmentBoardApi;

    @Inject
    private UserTestService userTestService;


    @Test
    public void shouldDepartmentUserBeAbleToApprovePost() {
        BoardRepresentation sampleBoard = postSampleBoard("department@poczta.fm");
        PostRepresentation samplePost = postSamplePost(sampleBoard.getId(), "poster@poczta.fm");

        transactionTemplate.execute(transactionStatus -> {
            userTestService.authenticateAs("department@poczta.fm");
            PostRepresentation postR = postApi.getPost(samplePost.getId());
            assertThat(postR.getActions(), Matchers.containsInAnyOrder(Action.EDIT, Action.ACCEPT, Action.REJECT, Action.SUSPEND));
            postApi.executeAction(samplePost.getId(), Action.ACCEPT, createSamplePost().setDescription("Corrected desc"));
            return null;
        });

        transactionTemplate.execute(transactionStatus -> {
            userTestService.authenticateAs("poster@poczta.fm");
            PostRepresentation postR = postApi.getPost(samplePost.getId());
            assertThat(postR.getActions(), Matchers.containsInAnyOrder(Action.EDIT));
            assertEquals(State.ACCEPTED, postR.getState());
            assertEquals("Corrected desc", postR.getDescription());
            return null;
        });
    }

    @Test
    public void shouldDepartmentUserBeAbleToRejectPost() {
        BoardRepresentation sampleBoard = postSampleBoard("department@poczta.fm");
        PostRepresentation samplePost = postSamplePost(sampleBoard.getId(), "poster@poczta.fm");

        transactionTemplate.execute(transactionStatus -> {
            userTestService.authenticateAs("department@poczta.fm");
            PostRepresentation postR = postApi.getPost(samplePost.getId());
            assertThat(postR.getActions(), Matchers.containsInAnyOrder(Action.EDIT, Action.ACCEPT, Action.REJECT, Action.SUSPEND));
            postApi.executeAction(samplePost.getId(), Action.REJECT, createSamplePost());
            return null;
        });

        transactionTemplate.execute(transactionStatus -> {
            userTestService.authenticateAs("poster@poczta.fm");
            PostRepresentation postR = postApi.getPost(samplePost.getId());
            assertThat(postR.getActions(), Matchers.containsInAnyOrder(Action.EDIT));
            assertEquals(State.REJECTED, postR.getState());
            return null;
        });
    }

    @Test
    public void shouldPosterBeAbleToCorrectPost() {
        BoardRepresentation sampleBoard = postSampleBoard("department@poczta.fm");
        PostRepresentation samplePost = postSamplePost(sampleBoard.getId(), "poster@poczta.fm");

        transactionTemplate.execute(transactionStatus -> {
            userTestService.authenticateAs("department@poczta.fm");
            PostRepresentation postR = postApi.getPost(samplePost.getId());
            assertThat(postR.getActions(), Matchers.containsInAnyOrder(Action.EDIT, Action.ACCEPT, Action.REJECT, Action.SUSPEND));
            postApi.executeAction(samplePost.getId(), Action.SUSPEND, createSamplePost());
            return null;
        });

        transactionTemplate.execute(transactionStatus -> {
            userTestService.authenticateAs("poster@poczta.fm");
            PostRepresentation postR = postApi.getPost(samplePost.getId());
            assertThat(postR.getActions(), Matchers.containsInAnyOrder(Action.EDIT));
            assertEquals(State.DRAFT, postR.getState());
            postApi.executeAction(samplePost.getId(), Action.EDIT, createSamplePost().setName("Corrected name"));
            return null;
        });

        transactionTemplate.execute(transactionStatus -> {
            userTestService.authenticateAs("department@poczta.fm");
            PostRepresentation postR = postApi.getPost(samplePost.getId());
            assertThat(postR.getActions(), Matchers.containsInAnyOrder(Action.EDIT, Action.ACCEPT, Action.REJECT, Action.SUSPEND));
            assertEquals(State.DRAFT, postR.getState());
            assertEquals("Corrected name", postR.getName());
            return null;
        });
    }

    private BoardRepresentation postSampleBoard(String user) {
        userTestService.authenticateAs(user);

        return transactionTemplate.execute(transactionStatus -> {
            BoardDTO boardDTO = new BoardDTO()
                .setName("ActionsIT Board")
                .setPurpose("Purpose")
                .setDepartment(new DepartmentDTO()
                    .setName("ActionsIT Department")
                    .setHandle("scp")
                    .setMemberCategories(ImmutableList.of("m1", "m2", "m3")))
                .setSettings(new BoardSettingsDTO()
                    .setHandle("scp")
                    .setPostCategories(ImmutableList.of("p1", "p2", "p3")));
            return departmentBoardApi.postBoard(boardDTO);
        });
    }

    private PostRepresentation postSamplePost(Long parentBoardId, String user) {
        PostDTO postDTO = createSamplePost();
        userTestService.authenticateAs(user);
        return transactionTemplate.execute(transactionStatus -> {
            BoardRepresentation boardR = departmentBoardApi.getBoard(parentBoardId);

            return postApi.postPost(boardR.getId(), postDTO);
        });
    }

    private PostDTO createSamplePost() {
        return new PostDTO()
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
            .setExistingRelation(RelationWithDepartment.FAMILY);
    }

}
