package hr.prism.board.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import hr.prism.board.api.ApiDataHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;

import javax.inject.Inject;

public class ApiTestConfiguration {

    private final MockMvc mockMvc;

    private final ObjectMapper objectMapper;

    @Inject
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public ApiTestConfiguration(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    @Bean
    public ApiDataHelper apiDataHelper() {
        return new ApiDataHelper(mockMvc, objectMapper);
    }

}
