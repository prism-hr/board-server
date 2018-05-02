package hr.prism.board.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hr.prism.board.dto.LoginDTO;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ApiHelper {

    private final MockMvc mockMvc;

    private final ObjectMapper objectMapper;

    public ApiHelper(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    @SuppressWarnings("SameParameterValue")
    String login(String email, String password) throws Exception {
        Map<String, String> response =
            objectMapper.readValue(
                mockMvc.perform(
                    post("/api/auth/login")
                        .contentType(APPLICATION_JSON_UTF8)
                        .content(objectMapper.writeValueAsString(
                            new LoginDTO()
                                .setEmail(email)
                                .setPassword(password))))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString(),
                new TypeReference<Map<String, String>>() {
                });


        return "Bearer " + response.get("token");
    }

}
