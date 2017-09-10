package hr.prism.board.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hr.prism.board.TestContext;
import hr.prism.board.exception.ExceptionCode;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.inject.Inject;
import java.util.Map;

@TestContext
@RunWith(SpringRunner.class)
public class ApiAdviceIT {

    @Inject
    private MockMvc mockMvc;

    @Inject
    private ObjectMapper objectMapper;

    @Test
    public void shouldHandleGeneralException() throws Exception {
        doRequest("generalException", MockMvcResultMatchers.status().isInternalServerError(), ExceptionCode.PROBLEM);
    }

    @Test
    public void shouldHandleRuntimeException() throws Exception {
        doRequest("runtimeException", MockMvcResultMatchers.status().isInternalServerError(), ExceptionCode.PROBLEM);
    }

    @Test
    public void shouldHandleApiException() throws Exception {
        doRequest("apiException", MockMvcResultMatchers.status().isInternalServerError(), ExceptionCode.MISSING_COMMENT);
    }

    @Test
    public void shouldHandleApiForbiddenException() throws Exception {
        doRequest("apiForbiddenException", MockMvcResultMatchers.status().isUnauthorized(), ExceptionCode.UNAUTHENTICATED_USER);
    }

    private void doRequest(String path, ResultMatcher resultMatcher, ExceptionCode exceptionCode) throws Exception {
        String response = mockMvc.perform(MockMvcRequestBuilders.get("/test/" + path))
            .andExpect(resultMatcher).andReturn().getResponse().getContentAsString();

        Map<String, String> deserializedResponse = objectMapper.readValue(response, new TypeReference<Map<String, String>>() {
        });

        Assert.assertEquals(exceptionCode.name(), deserializedResponse.get("exceptionCode"));
    }

}
