package hr.prism.board.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hr.prism.board.ApiTestContext;
import hr.prism.board.dto.DepartmentDTO;
import hr.prism.board.representation.ActionRepresentation;
import hr.prism.board.representation.DepartmentRepresentation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.Map;

import static hr.prism.board.enums.Action.*;
import static hr.prism.board.enums.MemberCategory.*;
import static hr.prism.board.enums.Scope.DEPARTMENT;
import static hr.prism.board.enums.Scope.UNIVERSITY;
import static hr.prism.board.enums.State.DRAFT;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
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
    private ApiTestHelper apiTestHelper;

    @Inject
    private ObjectMapper objectMapper;

    @Test
    public void createDepartment_success() throws Exception {
        LocalDateTime baseline = LocalDateTime.now().minusSeconds(1L);
        String authorization = apiTestHelper.login("alastair@prism.hr", "password");

        DepartmentRepresentation response =
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

        assertEquals(2L, response.getId().longValue());
        assertEquals(DEPARTMENT, response.getScope());
        assertEquals("department", response.getName());
        assertEquals("department summary", response.getSummary());
        assertEquals("department", response.getHandle());

        assertEquals(1L, response.getUniversity().getId().longValue());
        assertEquals(UNIVERSITY, response.getUniversity().getScope());
        assertEquals("university", response.getUniversity().getName());

        assertEquals(1L, response.getUniversity().getDocumentLogo().getId().longValue());
        assertEquals("cloudinary id", response.getUniversity().getDocumentLogo().getCloudinaryId());
        assertEquals("cloudinary url", response.getUniversity().getDocumentLogo().getCloudinaryUrl());
        assertEquals("file name", response.getUniversity().getDocumentLogo().getFileName());

        assertEquals(1L, response.getDocumentLogo().getId().longValue());
        assertEquals("cloudinary id", response.getDocumentLogo().getCloudinaryId());
        assertEquals("cloudinary url", response.getDocumentLogo().getCloudinaryUrl());
        assertEquals("file name", response.getDocumentLogo().getFileName());

        assertEquals(DRAFT, response.getState());

        assertThat(response.getMemberCategories()).containsExactly(
            UNDERGRADUATE_STUDENT, MASTER_STUDENT, RESEARCH_STUDENT, RESEARCH_STAFF);

        assertThat(
            response.getActions()
                .stream()
                .map(ActionRepresentation::getAction)
                .collect(toList()))
            .containsExactly(VIEW, EDIT, EXTEND, SUBSCRIBE);

        assertThat(response.getCreatedTimestamp()).isGreaterThan(baseline);
        assertThat(response.getUpdatedTimestamp()).isGreaterThan(baseline);
    }

    @Test
    public void createDepartment_failureWhenNotAuthenticated() throws Exception {
        Map<String, Object> response =
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

        assertEquals("/api/universities/1/departments", response.get("uri"));
        assertEquals(401, response.get("status"));
        assertEquals("Unauthorized", response.get("error"));
        assertEquals("UNAUTHENTICATED_USER", response.get("exceptionCode"));
    }

}
