package hr.prism.board.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import hr.prism.board.TestContext;
import hr.prism.board.domain.User;
import hr.prism.board.dto.*;
import hr.prism.board.enums.Notification;
import hr.prism.board.enums.OauthProvider;
import hr.prism.board.representation.UserRepresentation;
import hr.prism.board.service.AuthenticationService;
import hr.prism.board.service.TestNotificationService;
import io.jsonwebtoken.Jwts;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.inject.Inject;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@TestContext
@RunWith(SpringRunner.class)
public class AuthenticationApiIT extends AbstractIT {

    @Inject
    private MockMvc mockMvc;

    @Inject
    private ObjectMapper objectMapper;

    @Inject
    private AuthenticationService authenticationService;

    @Value("${environment}")
    String environment;

    @Value("${password.reset.timeout.seconds}")
    private Long passwordResetTimeoutSeconds;

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

        User user = userCacheService.findOneFresh(userRepresentation.getId());
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
            .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    public void shouldResetPasswordAndNotifyUser() throws Exception {
        testNotificationService.record();
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
        User user = transactionTemplate.execute(status -> userCacheService.findOneFresh(userId));
        String passwordResetUuid = user.getPasswordResetUuid();
        Assert.assertNotNull(passwordResetUuid);
        Assert.assertNotNull(user.getPasswordResetTimestamp());

        testNotificationService.verify(new TestNotificationService.NotificationInstance(Notification.RESET_PASSWORD_NOTIFICATION, user,
            ImmutableMap.of("recipient", "alastair", "environment", environment, "resetUuid", passwordResetUuid, "homeRedirect",
                serverUrl + "/redirect")));
        testNotificationService.stop();

        mockMvc.perform(
            MockMvcRequestBuilders.patch("/api/user/password")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(new UserPasswordDTO().setUuid(passwordResetUuid).setPassword("newpassword"))))
            .andExpect(MockMvcResultMatchers.status().isOk());

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(new LoginDTO().setEmail("alastair@prism.hr").setPassword("newpassword"))))
            .andExpect(MockMvcResultMatchers.status().isOk());

        user = transactionTemplate.execute(status -> userCacheService.findOneFresh(userId));
        Assert.assertNull(user.getPasswordResetUuid());
        Assert.assertNull(user.getPasswordResetTimestamp());
    }

    @Test
    public void shouldExpirePasswordReset() throws Exception {
        testNotificationService.record();
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
        User user = transactionTemplate.execute(status -> userCacheService.findOneFresh(userId));
        String passwordResetUuid = user.getPasswordResetUuid();
        Assert.assertNotNull(passwordResetUuid);
        Assert.assertNotNull(user.getPasswordResetTimestamp());

        testNotificationService.verify(new TestNotificationService.NotificationInstance(Notification.RESET_PASSWORD_NOTIFICATION, user,
            ImmutableMap.of("recipient", "alastair", "environment", environment, "resetUuid", passwordResetUuid, "homeRedirect",
                serverUrl + "/redirect")));
        testNotificationService.stop();

        TimeUnit.SECONDS.sleep(passwordResetTimeoutSeconds + 1);

        mockMvc.perform(
            MockMvcRequestBuilders.patch("/api/user/password")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(new UserPasswordDTO().setUuid(passwordResetUuid).setPassword("newpassword"))))
            .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    public void shouldSigninWithOauthProvider() throws Exception {
        String accessToken = (String) objectMapper.readValue(
            mockMvc.perform(
                MockMvcRequestBuilders.post("/api/auth/facebook")
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .content(objectMapper.writeValueAsString(
                        new SigninDTO().setClientId("clientId").setCode("code").setRedirectUri("redirectUri"))))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            Map.class).get("token");

        Long userId = objectMapper.readValue(
            mockMvc.perform(
                MockMvcRequestBuilders.get("/api/user")
                    .header("Authorization", "Bearer " + accessToken))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            UserRepresentation.class).getId();

        User user = userCacheService.findOneFresh(userId);
        Assert.assertEquals("alastair", user.getGivenName());
        Assert.assertEquals("knowles", user.getSurname());
        Assert.assertEquals("alastair@prism.hr", user.getEmail());
        Assert.assertEquals(OauthProvider.FACEBOOK, user.getOauthProvider());
        Assert.assertEquals("facebookId", user.getOauthAccountId());

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/auth/linkedin")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(
                    new SigninDTO().setClientId("clientId").setCode("code").setRedirectUri("redirectUri"))))
            .andExpect(MockMvcResultMatchers.status().isOk());

        user = userCacheService.findOneFresh(userId);
        Assert.assertEquals(OauthProvider.LINKEDIN, user.getOauthProvider());
        Assert.assertEquals("linkedinId", user.getOauthAccountId());
    }

    private void verifyAccessToken(String loginAccessToken, Long userId) {
        Assert.assertNotNull(loginAccessToken);
        Assert.assertEquals(userId,
            new Long(Long.parseLong(Jwts.parser().setSigningKey(authenticationService.getJwsSecret()).parseClaimsJws(loginAccessToken).getBody().getSubject())));
    }

}
