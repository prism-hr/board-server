package hr.prism.board.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hr.prism.board.ApiTestContext;
import hr.prism.board.dto.DepartmentDTO;
import hr.prism.board.representation.ActionRepresentation;
import hr.prism.board.representation.BoardRepresentation;
import hr.prism.board.representation.DepartmentRepresentation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static hr.prism.board.enums.Action.*;
import static hr.prism.board.enums.MemberCategory.*;
import static hr.prism.board.enums.Scope.*;
import static hr.prism.board.enums.State.ACCEPTED;
import static hr.prism.board.enums.State.DRAFT;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ApiTestContext
@RunWith(SpringRunner.class)
@Sql("classpath:data/departmentApi_setUp.sql")
@Sql(value = "classpath:data/departmentApi_tearDown.sql", executionPhase = AFTER_TEST_METHOD)
public class NewDepartmentApiIT {

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
    public void createDepartment_success() throws Exception {
        LocalDateTime baseline = LocalDateTime.now().minusSeconds(1L);
        String authorization = apiTestHelper.login("alastair@prism.hr", "password");

        DepartmentRepresentation department =
            objectMapper.readValue(
                mockMvc.perform(
                    post("/api/universities/1/departments")
                        .contentType(APPLICATION_JSON_UTF8)
                        .header("Authorization", authorization)
                        .content(objectMapper.writeValueAsString(
                            new DepartmentDTO()
                                .setName("department")
                                .setSummary("department summary"))))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString(),
                DepartmentRepresentation.class);

        Long departmentId = department.getId();
        assertNotNull(departmentId);

        assertEquals(DEPARTMENT, department.getScope());
        assertEquals("department", department.getName());
        assertEquals("department summary", department.getSummary());
        assertEquals("department", department.getHandle());

        assertEquals(1L, department.getUniversity().getId().longValue());
        assertEquals(UNIVERSITY, department.getUniversity().getScope());
        assertEquals("university", department.getUniversity().getName());
        assertEquals("university", department.getUniversity().getHandle());

        assertEquals(1L, department.getUniversity().getDocumentLogo().getId().longValue());
        assertEquals("cloudinary id", department.getUniversity().getDocumentLogo().getCloudinaryId());
        assertEquals("cloudinary url", department.getUniversity().getDocumentLogo().getCloudinaryUrl());
        assertEquals("file name", department.getUniversity().getDocumentLogo().getFileName());

        assertEquals(1L, department.getDocumentLogo().getId().longValue());
        assertEquals("cloudinary id", department.getDocumentLogo().getCloudinaryId());
        assertEquals("cloudinary url", department.getDocumentLogo().getCloudinaryUrl());
        assertEquals("file name", department.getDocumentLogo().getFileName());

        assertEquals(DRAFT, department.getState());

        assertThat(department.getMemberCategories()).containsExactly(
            UNDERGRADUATE_STUDENT, MASTER_STUDENT, RESEARCH_STUDENT, RESEARCH_STAFF);

        assertThat(
            department.getActions()
                .stream()
                .map(ActionRepresentation::getAction)
                .collect(toList()))
            .containsExactly(VIEW, EDIT, EXTEND, SUBSCRIBE);

        assertThat(department.getCreatedTimestamp()).isGreaterThan(baseline);
        assertThat(department.getUpdatedTimestamp()).isGreaterThan(baseline);

        List<BoardRepresentation> boards =
            objectMapper.readValue(
                mockMvc.perform(
                    get("/api/boards?parentId=" + departmentId + " &includePublic=true")
                        .contentType(APPLICATION_JSON_UTF8)
                        .header("Authorization", authorization))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString(),
                new TypeReference<List<BoardRepresentation>>() {
                });

        assertThat(boards).hasSize(2);

        BoardRepresentation board0 = boards.get(0);
        assertNotNull(board0.getId());
        assertEquals(BOARD, board0.getScope());
        assertEquals("Career Opportunities", board0.getName());
        assertEquals("career-opportunities", board0.getHandle());
        assertEquals(ACCEPTED, board0.getState());
        assertThat(board0.getPostCategories()).containsExactly("Employment", "Internship", "Volunteering");

        assertThat(
            board0.getActions()
                .stream()
                .map(ActionRepresentation::getAction)
                .collect(toList()))
            .containsExactly(VIEW, EDIT, EXTEND, REJECT);

        assertThat(board0.getCreatedTimestamp()).isGreaterThan(baseline);
        assertThat(board0.getUpdatedTimestamp()).isGreaterThan(baseline);

        BoardRepresentation board1 = boards.get(1);
        assertNotNull(board1.getId());
        assertEquals(BOARD, board1.getScope());
        assertEquals("Research Opportunities", board1.getName());
        assertEquals("research-opportunities", board1.getHandle());
        assertEquals(ACCEPTED, board1.getState());
        assertThat(board1.getPostCategories()).containsExactly("MRes", "PhD", "Postdoc");

        assertThat(
            board1.getActions()
                .stream()
                .map(ActionRepresentation::getAction)
                .collect(toList()))
            .containsExactly(VIEW, EDIT, EXTEND, REJECT);

        assertThat(board1.getCreatedTimestamp()).isGreaterThan(baseline);
        assertThat(board1.getUpdatedTimestamp()).isGreaterThan(baseline);
    }

    @Test
    public void createDepartment_failureWhenNotAuthenticated() throws Exception {
        Map<String, Object> error =
            objectMapper.readValue(
                mockMvc.perform(
                    post("/api/universities/1/departments")
                        .contentType(APPLICATION_JSON_UTF8)
                        .content(objectMapper.writeValueAsString(
                            new DepartmentDTO()
                                .setName("department")
                                .setSummary("department summary"))))
                    .andExpect(status().isUnauthorized())
                    .andReturn().getResponse().getContentAsString(),
                new TypeReference<Map<String, Object>>() {
                });

        assertEquals("/api/universities/1/departments", error.get("uri"));
        assertEquals(401, error.get("status"));
        assertEquals("Unauthorized", error.get("error"));
        assertEquals("UNAUTHENTICATED_USER", error.get("exceptionCode"));
    }

}
