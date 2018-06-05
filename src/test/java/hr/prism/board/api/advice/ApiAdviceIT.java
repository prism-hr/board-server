package hr.prism.board.api.advice;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hr.prism.board.ApiTestContext;
import hr.prism.board.exception.ExceptionCode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.inject.Inject;
import java.util.Map;

import static hr.prism.board.exception.ExceptionCode.*;
import static java.lang.Integer.parseInt;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertEquals;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@ApiTestContext
@RunWith(SpringRunner.class)
@Sql(scripts = {"classpath:data/tearDown.sql", "classpath:data/api_setUp.sql"})
@Sql(scripts = {"classpath:data/tearDown.sql"}, executionPhase = AFTER_TEST_METHOD)
public class ApiAdviceIT {

    @Inject
    private MockMvc mockMvc;

    @Inject
    private ObjectMapper objectMapper;

    @Test
    public void handleThrowable() throws Exception {
        doRequest("throwable", INTERNAL_SERVER_ERROR, UNKNOWN);
    }

    @Test
    public void handleException() throws Exception {
        doRequest("exception", INTERNAL_SERVER_ERROR, UNKNOWN);
    }

    @Test
    public void handleRuntimeException() throws Exception {
        doRequest("runtimeException", INTERNAL_SERVER_ERROR, UNKNOWN);
    }

    @Test
    public void handleBoardDuplicateException() throws Exception {
        doRequest("boardDuplicateException", CONFLICT, DUPLICATE_RESOURCE, singletonMap("id", "1"));
    }

    @Test
    public void handleBoardException() throws Exception {
        doRequest("boardException", INTERNAL_SERVER_ERROR, MISSING_COMMENT);
    }

    @Test
    public void handleBoardForbiddenException() throws Exception {
        doRequest("boardForbiddenException", UNAUTHORIZED, UNAUTHENTICATED_USER);
    }

    @Test
    public void handleBoardNotFoundException() throws Exception {
        doRequest("boardNotFoundException", NOT_FOUND, MISSING_RESOURCE, singletonMap("id", "1"));
    }

    @Test
    public void handleAccessDeniedException() throws Exception {
        doRequest("accessDeniedException", UNAUTHORIZED, UNAUTHENTICATED_USER);
    }

    private void doRequest(String path, HttpStatus status, ExceptionCode exceptionCode) throws Exception {
        doRequest(path, status, exceptionCode, emptyMap());
    }

    private void doRequest(String path, HttpStatus status, ExceptionCode exceptionCode, Map<String, String> properties)
        throws Exception {
        Map<String, String> response =
            objectMapper.readValue(
                mockMvc.perform(
                    get("/test/" + path))
                    .andExpect(MockMvcResultMatchers.status().is(status.value()))
                    .andReturn()
                    .getResponse().getContentAsString(),
                new TypeReference<Map<String, String>>() {
                });

        assertEquals("/test/" + path, response.get("uri"));
        assertEquals(status.value(), parseInt(response.get("status")));
        assertEquals(status.getReasonPhrase(), response.get("error"));
        assertEquals(exceptionCode.name(), response.get("exceptionCode"));
        properties.forEach((key, value) -> assertEquals(value, response.get(key)));
    }

}
