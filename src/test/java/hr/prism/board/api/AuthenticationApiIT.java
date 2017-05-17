package hr.prism.board.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import hr.prism.board.TestContext;
import hr.prism.board.domain.User;
import hr.prism.board.dto.LoginDTO;
import hr.prism.board.dto.RegisterDTO;
import hr.prism.board.representation.UserRepresentation;
import hr.prism.board.service.TestUserService;
import hr.prism.board.service.UserService;
import io.jsonwebtoken.Jwts;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.inject.Inject;
import java.util.Date;

@TestContext
@RunWith(SpringRunner.class)
public class AuthenticationApiIT {
    
    @Inject
    private MockMvc mockMvc;
    
    @Inject
    private ObjectMapper objectMapper;
    
    @Inject
    private UserService userService;
    
    @Inject
    private TestUserService testUserService;
    
    @Test
    public void shouldRegisterAndAuthenticateUser() throws Exception {
        RegisterDTO registerDTO = new RegisterDTO().setGivenName("alastair").setSurname("knowles").setEmail("alastair@prism.hr").setPassword("password");
        MockHttpServletResponse registerResponse =
            mockMvc.perform(
                MockMvcRequestBuilders.post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse();
    
        UserRepresentation registerResponseBody = objectMapper.readValue(registerResponse.getContentAsString(), UserRepresentation.class);
        User user = userService.findOne(registerResponseBody.getId());
        Long userId = user.getId();
    
        verifyAccessToken(registerResponse, userId);
        Assert.assertEquals("alastair", registerResponseBody.getGivenName());
        Assert.assertEquals("knowles", registerResponseBody.getSurname());
        Assert.assertEquals("alastair@prism.hr", registerResponseBody.getEmail());
    
        Assert.assertEquals(DigestUtils.sha256Hex("password"), user.getPassword());
    
        Thread.currentThread().sleep(1000);
        LoginDTO loginDTO = new LoginDTO().setEmail("alastair@prism.hr").setPassword("password");
        MockHttpServletResponse loginResponse =
            mockMvc.perform(
                MockMvcRequestBuilders.post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse();
    
        verifyAccessToken(loginResponse, userId);
        String loginAccessToken = loginResponse.getHeader("Authorization").replaceFirst("Bearer", "");
    
        UserRepresentation loginResponseBody = objectMapper.readValue(loginResponse.getContentAsString(), UserRepresentation.class);
        Assert.assertEquals("alastair@prism.hr", loginResponseBody.getEmail());
    
        Thread.currentThread().sleep(1000);
        MockHttpServletResponse userResponse =
            mockMvc.perform(
                MockMvcRequestBuilders.get("/api/user")
                    .header("Authorization", "Bearer" + loginAccessToken))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse();
    
        verifyAccessToken(userResponse, userId);
        String userAccessToken = userResponse.getHeader("Authorization").replaceFirst("Bearer", "");
    
        UserRepresentation userResponseBody = objectMapper.readValue(userResponse.getContentAsString(), UserRepresentation.class);
        Assert.assertEquals("alastair@prism.hr", userResponseBody.getEmail());
        Assert.assertNotEquals(loginAccessToken, userAccessToken);
    
        Date loginAccessTokenExpiration = Jwts.parser().setSigningKey("secret").parseClaimsJws(loginAccessToken).getBody().getExpiration();
        Date userAccessTokenExpiration = Jwts.parser().setSigningKey("secret").parseClaimsJws(userAccessToken).getBody().getExpiration();
        Assert.assertTrue(userAccessTokenExpiration.after(loginAccessTokenExpiration));
    
        testUserService.unauthenticate();
    
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/posts"))
            .andExpect(MockMvcResultMatchers.status().isOk());
    
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/user"))
            .andExpect(MockMvcResultMatchers.status().isForbidden());
    }
    
    private void verifyAccessToken(MockHttpServletResponse response, Long userId) {
        String authorization = response.getHeader("Authorization");
        Assert.assertNotNull(authorization);
        Assert.assertTrue(authorization.startsWith("Bearer"));
        Assert.assertEquals(userId,
            new Long(Long.parseLong(Jwts.parser().setSigningKey("secret").parseClaimsJws(authorization.replaceFirst("Bearer", "")).getBody().getSubject())));
    }
    
}
