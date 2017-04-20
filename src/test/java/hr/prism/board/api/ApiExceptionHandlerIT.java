package hr.prism.board.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hr.prism.board.ApplicationConfiguration;
import hr.prism.board.exception.ExceptionCode;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.inject.Inject;
import java.util.Map;

@AutoConfigureMockMvc
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {ApplicationConfiguration.class})
@TestPropertySource(value = {"classpath:application.properties", "classpath:test.properties"})
public class ApiExceptionHandlerIT {
    
    @Inject
    private MockMvc mockMvc;
    
    @Inject
    private ObjectMapper objectMapper;
    
    @Test
    public void shouldHandleGeneralException() throws Exception {
        doRequest("generalException", MockMvcResultMatchers.status().isInternalServerError(), ExceptionCode.PROBLEM);
    }
    
    @Test
    public void shouldHandleApiException() throws Exception {
        doRequest("apiException", MockMvcResultMatchers.status().isUnprocessableEntity(), ExceptionCode.DUPLICATE_DEPARTMENT);
    }
    
    @Test
    public void shouldHandleApiForbiddenException() throws Exception {
        doRequest("apiForbiddenException", MockMvcResultMatchers.status().isForbidden(), ExceptionCode.UNAUTHENTICATED_USER);
    }
    
    private void doRequest(String path, ResultMatcher resultMatcher, ExceptionCode exceptionCode) throws Exception {
        String response = mockMvc.perform(MockMvcRequestBuilders.get("/test/" + path))
            .andExpect(resultMatcher).andReturn().getResponse().getContentAsString();
        
        Map<String, String> deserializedResponse = objectMapper.readValue(response, new TypeReference<Map<String, String>>() {
        });
        
        Assert.assertEquals(exceptionCode.name(), deserializedResponse.get("exceptionCode"));
    }
    
}
