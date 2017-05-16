package hr.prism.board.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import hr.prism.board.TestContext;
import hr.prism.board.dto.RegisterDTO;
import hr.prism.board.representation.UserRepresentation;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.inject.Inject;

@TestContext
@RunWith(SpringRunner.class)
public class AuthenticationApiIT {
    
    @Inject
    private MockMvc mockMvc;
    
    @Inject
    private ObjectMapper objectMapper;
    
    @Test
    public void shouldRegisterAndAuthenticateUser() throws Exception {
        RegisterDTO registerDTO = new RegisterDTO().setGivenName("alastair").setSurname("knowles").setEmail("alastair@prism.hr").setPassword("password");
        MockHttpServletResponse response =
            mockMvc.perform(MockMvcRequestBuilders.post("auth/register"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse();
    
        String authorization = response.getHeader("Authorization");
        Assert.assertTrue(authorization.startsWith("Bearer"));
    
        UserRepresentation userRepresentation = objectMapper.readValue(response.getContentAsString(), UserRepresentation.class);
        Assert.assertEquals(registerDTO.getGivenName(), userRepresentation.getGivenName());
        Assert.assertEquals(registerDTO.getSurname(), userRepresentation.getSurname());
        Assert.assertEquals(registerDTO.getEmail(), userRepresentation.getEmail());
    }
    
}
