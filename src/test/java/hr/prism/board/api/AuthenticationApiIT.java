package hr.prism.board.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import hr.prism.board.TestContext;
import hr.prism.board.domain.User;
import hr.prism.board.dto.LoginDTO;
import hr.prism.board.dto.RegisterDTO;
import hr.prism.board.dto.ResetPasswordDTO;
import hr.prism.board.representation.UserRepresentation;
import hr.prism.board.service.NotificationService;
import hr.prism.board.service.TestNotificationService;
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
import java.time.LocalDateTime;
import java.util.Map;

@TestContext
@RunWith(SpringRunner.class)
public class AuthenticationApiIT extends AbstractIT {
    
    @Inject
    private MockMvc mockMvc;
    
    @Inject
    private ObjectMapper objectMapper;
    
    @Inject
    private UserService userService;
    
    @Inject
    private TestUserService testUserService;
    
    @Inject
    private TestNotificationService testNotificationService;
    
    @Test
    public void shouldRegisterAndAuthenticateUser() throws Exception {
        RegisterDTO registerDTO = new RegisterDTO().setGivenName("alastair").setSurname("knowles").setEmail("alastair@prism.hr").setPassword("password");
        MockHttpServletResponse registerResponse =
            mockMvc.perform(
                MockMvcRequestBuilders.post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse();
        
        String loginAccessToken = (String) objectMapper.readValue(registerResponse.getContentAsString(), Map.class).get("token");
        
        MockHttpServletResponse userResponse =
            mockMvc.perform(
                MockMvcRequestBuilders.get("/api/user")
                    .header("Authorization", "Bearer " + loginAccessToken))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse();
        UserRepresentation userRepresentation = objectMapper.readValue(userResponse.getContentAsString(), UserRepresentation.class);
        
        User user = userService.findOne(userRepresentation.getId());
        Long userId = user.getId();
        
        verifyAccessToken(loginAccessToken, userId);
        Assert.assertEquals("alastair", userRepresentation.getGivenName());
        Assert.assertEquals("knowles", userRepresentation.getSurname());
        Assert.assertEquals("alastair@prism.hr", userRepresentation.getEmail());
        
        Assert.assertEquals(DigestUtils.sha256Hex("password"), user.getPassword());
        
        Thread.sleep(1000);
        LoginDTO loginDTO = new LoginDTO().setEmail("alastair@prism.hr").setPassword("password");
        MockHttpServletResponse loginResponse =
            mockMvc.perform(
                MockMvcRequestBuilders.post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse();
        
        loginAccessToken = (String) objectMapper.readValue(loginResponse.getContentAsString(), Map.class).get("token");
        verifyAccessToken(loginAccessToken, userId);
        
        Thread.sleep(1000);
        userResponse =
            mockMvc.perform(
                MockMvcRequestBuilders.get("/api/user")
                    .header("Authorization", "Bearer " + loginAccessToken))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse();
        
        UserRepresentation userResponseBody = objectMapper.readValue(userResponse.getContentAsString(), UserRepresentation.class);
        Assert.assertEquals("alastair@prism.hr", userResponseBody.getEmail());
        
        testUserService.unauthenticate();
        
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/posts"))
            .andExpect(MockMvcResultMatchers.status().isOk());
        
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/user"))
            .andExpect(MockMvcResultMatchers.status().isForbidden());
    }
    
    @Test
    public void shouldResetPasswordAndNotifyUser() throws Exception {
        RegisterDTO registerDTO = new RegisterDTO().setGivenName("alastair").setSurname("knowles").setEmail("alastair@prism.hr").setPassword("password");
        String accessToken = (String) objectMapper.readValue(
            mockMvc.perform(
                MockMvcRequestBuilders.post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            Map.class).get("token");
        
        MockHttpServletResponse userResponse =
            mockMvc.perform(
                MockMvcRequestBuilders.get("/api/user")
                    .header("Authorization", "Bearer " + accessToken))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse();
        UserRepresentation userR = objectMapper.readValue(userResponse.getContentAsString(), UserRepresentation.class);
        
        ResetPasswordDTO resetPasswordDTO = new ResetPasswordDTO().setEmail("alastair@prism.hr");
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/auth/resetPassword")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(resetPasswordDTO)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andReturn();
    
        Long userId = userR.getId();
        User user = userService.findOne(userId);
        Assert.assertNotNull(user.getTemporaryPassword());
        
        LocalDateTime temporaryPasswordExpiryTimestamp = user.getTemporaryPasswordExpiryTimestamp();
        Assert.assertNotNull(temporaryPasswordExpiryTimestamp);
        Assert.assertTrue(temporaryPasswordExpiryTimestamp.isAfter(LocalDateTime.now()));
        testNotificationService.verify(new NotificationService.Notification("reset_password", "admin@prism.hr", "alastair@prism.hr",
            ImmutableMap.of("firstName", "alastair", "temporaryPassword", "defined", "redirectUrl", "http://localhost:8080/redirect?path=login")));
    
        // Set the temporary password to something that we know
        transactionTemplate.execute(status -> userService.findOne(userId).setTemporaryPassword(DigestUtils.sha256Hex("temporary")));
        
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(new LoginDTO().setEmail("alastair@prism.hr").setPassword("temporary"))))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }
    
    private void verifyAccessToken(String loginAccessToken, Long userId) {
        Assert.assertNotNull(loginAccessToken);
        Assert.assertEquals(userId,
            new Long(Long.parseLong(Jwts.parser().setSigningKey("secret").parseClaimsJws(loginAccessToken).getBody().getSubject())));
    }
    
}
