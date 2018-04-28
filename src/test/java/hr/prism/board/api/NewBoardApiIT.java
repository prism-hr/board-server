package hr.prism.board.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import hr.prism.board.ApiTestContext;
import hr.prism.board.dto.BoardDTO;
import hr.prism.board.representation.ActionRepresentation;
import hr.prism.board.representation.BoardRepresentation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import javax.inject.Inject;
import java.time.LocalDateTime;

import static hr.prism.board.enums.Action.*;
import static hr.prism.board.enums.Scope.DEPARTMENT;
import static hr.prism.board.enums.Scope.UNIVERSITY;
import static hr.prism.board.enums.State.ACCEPTED;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ApiTestContext
@RunWith(SpringRunner.class)
@Sql("classpath:data/boardApi_setUp.sql")
@Sql(value = "classpath:data/boardApi_tearDown.sql", executionPhase = AFTER_TEST_METHOD)
public class NewBoardApiIT {

    @Inject
    private MockMvc mockMvc;

    @Inject
    private ObjectMapper objectMapper;

    private ApiTestHelper apiTestHelper;

    @Before
    public void setUp() {
        apiTestHelper = new ApiTestHelper(mockMvc, objectMapper);
    }

    @Test
    public void createBoard_success() throws Exception {
        LocalDateTime baseline = LocalDateTime.now().minusSeconds(1L);
        String authorization = apiTestHelper.login("alastair@prism.hr", "password");

        BoardRepresentation board =
            objectMapper.readValue(
                mockMvc.perform(
                    post("/api/departments/2/boards")
                        .contentType(APPLICATION_JSON_UTF8)
                        .header("Authorization", authorization)
                        .content(objectMapper.writeValueAsString(
                            new BoardDTO()
                                .setName("new board")
                                .setPostCategories(ImmutableList.of("new category 1", "new category 2")))))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString(),
                BoardRepresentation.class);

        assertNotNull(board.getId());
        assertEquals("new board", board.getName());
        assertEquals("new-board", board.getHandle());

        assertEquals(2, board.getDepartment().getId().longValue());
        assertEquals(DEPARTMENT, board.getDepartment().getScope());
        assertEquals("department", board.getDepartment().getName());
        assertEquals("department", board.getDepartment().getHandle());

        assertEquals(1L, board.getDepartment().getDocumentLogo().getId().longValue());
        assertEquals("cloudinary id", board.getDepartment().getDocumentLogo().getCloudinaryId());
        assertEquals("cloudinary url", board.getDepartment().getDocumentLogo().getCloudinaryUrl());
        assertEquals("file name", board.getDepartment().getDocumentLogo().getFileName());

        assertEquals(1, board.getDepartment().getUniversity().getId().longValue());
        assertEquals(UNIVERSITY, board.getDepartment().getUniversity().getScope());
        assertEquals("university", board.getDepartment().getUniversity().getName());
        assertEquals("university", board.getDepartment().getUniversity().getHandle());

        assertEquals(1L, board.getDepartment().getUniversity().getDocumentLogo().getId().longValue());
        assertEquals("cloudinary id",
            board.getDepartment().getUniversity().getDocumentLogo().getCloudinaryId());
        assertEquals("cloudinary url",
            board.getDepartment().getUniversity().getDocumentLogo().getCloudinaryUrl());
        assertEquals("file name", board.getDepartment().getUniversity().getDocumentLogo().getFileName());

        assertThat(board.getPostCategories()).containsExactly("new category 1", "new category 2");

        assertThat(
            board.getActions()
                .stream()
                .map(ActionRepresentation::getAction)
                .collect(toList()))
            .containsExactly(VIEW, EDIT, EXTEND, REJECT);

        assertEquals(ACCEPTED, board.getState());
        assertThat(board.getCreatedTimestamp()).isGreaterThan(baseline);
        assertThat(board.getUpdatedTimestamp()).isGreaterThan(baseline);
    }

}
