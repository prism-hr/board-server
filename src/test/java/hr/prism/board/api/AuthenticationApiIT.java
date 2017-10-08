package hr.prism.board.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import hr.prism.board.TestContext;
import hr.prism.board.TestHelper;
import hr.prism.board.domain.Resource;
import hr.prism.board.domain.User;
import hr.prism.board.dto.*;
import hr.prism.board.enums.*;
import hr.prism.board.exception.BoardForbiddenException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.exception.ExceptionUtils;
import hr.prism.board.representation.BoardRepresentation;
import hr.prism.board.representation.DepartmentRepresentation;
import hr.prism.board.representation.PostRepresentation;
import hr.prism.board.representation.UserRepresentation;
import hr.prism.board.service.AuthenticationService;
import hr.prism.board.service.TestNotificationService;
import hr.prism.board.util.BoardUtils;
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
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
            ImmutableMap.<String, String>builder().put("recipient", "alastair").put("environment", environment).put("resetUuid", passwordResetUuid)
                .put("homeRedirect", serverUrl + "/redirect").build()));
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
            ImmutableMap.<String, String>builder().put("recipient", "alastair").put("environment", environment).put("resetUuid", passwordResetUuid)
                .put("homeRedirect", serverUrl + "/redirect").build()));
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

    @Test
    public void shouldReconcileAuthenticationsWithInvitations() {
        Long userId1 = testUserService.authenticate().getId();
        BoardRepresentation boardR1 = transactionTemplate.execute(status -> boardApi.postBoard(TestHelper.smallSampleBoard()));
        DepartmentRepresentation departmentR1 = boardR1.getDepartment();
        Long departmentId1 = departmentR1.getId();
        Long boardId1 = boardR1.getId();

        transactionTemplate.execute(status -> departmentApi.postMembers(departmentId1, Arrays.asList(
            new UserRoleDTO().setUser(new UserDTO().setGivenName("member1").setSurname("member1").setEmail("member1@member1.com")).setRole(Role.MEMBER),
            new UserRoleDTO().setUser(new UserDTO().setGivenName("member2").setSurname("member2").setEmail("member2@member2.com")).setRole(Role.MEMBER),
            new UserRoleDTO().setUser(new UserDTO().setGivenName("member3").setSurname("member3").setEmail("member3@member3.com")).setRole(Role.MEMBER))));

        testNotificationService.record();
        PostRepresentation postR1 = transactionTemplate.execute(status -> postApi.postPost(boardId1, TestHelper.smallSamplePost()));
        transactionTemplate.execute(status -> {
            postService.publishAndRetirePosts();
            return null;
        });

        String postName1 = postR1.getName();
        String boardName1 = boardR1.getName();
        String departmentName1 = departmentR1.getName();

        User member1 = transactionTemplate.execute(status -> userRepository.findByEmail("member1@member1.com"));
        User member2 = transactionTemplate.execute(status -> userRepository.findByEmail("member2@member2.com"));
        User member3 = transactionTemplate.execute(status -> userRepository.findByEmail("member3@member3.com"));

        Resource department1 = transactionTemplate.execute(status -> resourceService.findOne(departmentId1));
        String department1memberRole1Uuid = transactionTemplate.execute(status -> userRoleService.findByResourceAndUserAndRole(department1, member1, Role.MEMBER)).getUuid();
        String department1memberRole2Uuid = transactionTemplate.execute(status -> userRoleService.findByResourceAndUserAndRole(department1, member2, Role.MEMBER)).getUuid();
        String department1memberRole3Uuid = transactionTemplate.execute(status -> userRoleService.findByResourceAndUserAndRole(department1, member3, Role.MEMBER)).getUuid();

        String parentRedirect1 = serverUrl + "/redirect?resource=" + boardId1;
        String resourceRedirect1 = serverUrl + "/redirect?resource=" + postR1.getId();
        User user1 = transactionTemplate.execute(status -> userCacheService.findOne(userId1));

        testNotificationService.stop();
        testNotificationService.verify(
            new TestNotificationService.NotificationInstance(Notification.PUBLISH_POST_NOTIFICATION, user1,
                ImmutableMap.<String, String>builder().put("recipient", user1.getGivenName()).put("department", departmentName1).put("board", boardName1)
                    .put("post", postName1).put("resourceRedirect", resourceRedirect1).put("modal", "Login").build()),
            new TestNotificationService.NotificationInstance(Notification.PUBLISH_POST_MEMBER_NOTIFICATION, member1,
                ImmutableMap.<String, String>builder().put("recipient", "member1").put("department", departmentName1).put("board", boardName1).put("post", postName1)
                    .put("organization", "organization name").put("summary", "summary").put("resourceRedirect", resourceRedirect1)
                    .put("invitationUuid", department1memberRole1Uuid).put("modal", "Register").put("parentRedirect", parentRedirect1)
                    .put("recipientUuid", member1.getUuid()).build()),
            new TestNotificationService.NotificationInstance(Notification.PUBLISH_POST_MEMBER_NOTIFICATION, member2,
                ImmutableMap.<String, String>builder().put("recipient", "member2").put("department", departmentName1).put("board", boardName1).put("post", postName1)
                    .put("organization", "organization name").put("summary", "summary").put("resourceRedirect", resourceRedirect1)
                    .put("invitationUuid", department1memberRole2Uuid).put("modal", "Register").put("parentRedirect", parentRedirect1)
                    .put("recipientUuid", member2.getUuid()).build()),
            new TestNotificationService.NotificationInstance(Notification.PUBLISH_POST_MEMBER_NOTIFICATION, member3,
                ImmutableMap.<String, String>builder().put("recipient", "member3").put("department", departmentName1).put("board", boardName1).put("post", postName1)
                    .put("organization", "organization name").put("summary", "summary").put("resourceRedirect", resourceRedirect1)
                    .put("invitationUuid", department1memberRole3Uuid).put("modal", "Register").put("parentRedirect", parentRedirect1)
                    .put("recipientUuid", member3.getUuid()).build()));

        Long postId1 = postR1.getId();
        transactionTemplate.execute(status -> authenticationApi.register(
            new RegisterDTO().setUuid(department1memberRole1Uuid).setGivenName("member1").setSurname("member1").setEmail("member1@member1.com").setPassword("password1")));
        testUserService.setAuthentication(member1.getId());
        departmentApi.putMembershipUpdate(departmentId1, new UserRoleDTO().setUser(new UserDTO().setGender(Gender.FEMALE).setAgeRange(AgeRange.TWENTYFIVE_TWENTYNINE)
            .setLocationNationality(new LocationDTO().setName("United Kingdom").setDomicile("GBR").setGoogleId("googleId").setLatitude(BigDecimal.ONE).setLongitude(BigDecimal.ONE))));

        postR1 = transactionTemplate.execute(status -> postApi.getPost(postId1, TestHelper.mockHttpServletRequest("ip1")));
        Assert.assertNotNull(postR1.getReferral());

        transactionTemplate.execute(status -> authenticationApi.register(
            new RegisterDTO().setUuid(department1memberRole2Uuid).setGivenName("member4").setSurname("member4").setEmail("member4@member4.com").setPassword("password4")));
        testUserService.setAuthentication(member2.getId());
        departmentApi.putMembershipUpdate(departmentId1, new UserRoleDTO().setUser(new UserDTO().setGender(Gender.FEMALE).setAgeRange(AgeRange.TWENTYFIVE_TWENTYNINE)
            .setLocationNationality(new LocationDTO().setName("United Kingdom").setDomicile("GBR").setGoogleId("googleId").setLatitude(BigDecimal.ONE).setLongitude(BigDecimal.ONE))));

        postR1 = transactionTemplate.execute(status -> postApi.getPost(postId1, TestHelper.mockHttpServletRequest("ip4")));
        Assert.assertNotNull(postR1.getReferral());

        testUserService.setAuthentication(userId1);
        List<String> emails1 = resourceApi.getUserRoles(Scope.DEPARTMENT, departmentId1, null)
            .getMembers().stream().map(userRole -> userRole.getUser().getEmail()).collect(Collectors.toList());
        verifyContains(emails1, BoardUtils.obfuscateEmail("member1@member1.com"),
            BoardUtils.obfuscateEmail("member4@member4.com"), BoardUtils.obfuscateEmail("member3@member3.com"));

        transactionTemplate.execute(status -> ExceptionUtils.verifyException(BoardForbiddenException.class,
            () -> authenticationApi.register(
                new RegisterDTO().setUuid(department1memberRole3Uuid).setGivenName("member1").setSurname("member1").setEmail("member1@member1.com").setPassword("password1")),
            ExceptionCode.DUPLICATE_USER, status));

        transactionTemplate.execute(status -> ExceptionUtils.verifyException(BoardForbiddenException.class,
            () -> authenticationApi.register(
                new RegisterDTO().setUuid(department1memberRole1Uuid).setGivenName("member1").setSurname("member1").setEmail("member1@member1.com").setPassword("password1")),
            ExceptionCode.DUPLICATE_AUTHENTICATION, status));

        Long userId2 = testUserService.authenticate().getId();
        BoardRepresentation boardR2 = transactionTemplate.execute(status -> boardApi.postBoard(
            new BoardDTO()
                .setName("board2")
                .setDepartment(new DepartmentDTO()
                    .setName("department2"))));

        DepartmentRepresentation departmentR2 = boardR2.getDepartment();
        Long departmentId2 = departmentR2.getId();
        Long boardId2 = boardR2.getId();

        transactionTemplate.execute(status -> departmentApi.postMembers(departmentId2, Arrays.asList(
            new UserRoleDTO().setUser(new UserDTO().setGivenName("member1").setSurname("member1").setEmail("member1@member1.com")).setRole(Role.MEMBER),
            new UserRoleDTO().setUser(new UserDTO().setGivenName("member3").setSurname("member3").setEmail("member3@member3.com")).setRole(Role.MEMBER))));

        testNotificationService.record();
        PostRepresentation postR2 = transactionTemplate.execute(status -> postApi.postPost(boardId2, TestHelper.smallSamplePost()));
        transactionTemplate.execute(status -> {
            postService.publishAndRetirePosts();
            return null;
        });

        String postName2 = postR2.getName();
        String boardName2 = boardR2.getName();
        String departmentName2 = departmentR2.getName();

        Resource department2 = transactionTemplate.execute(status -> resourceService.findOne(departmentId2));
        String department2memberRole1Uuid = transactionTemplate.execute(status -> userRoleService.findByResourceAndUserAndRole(department2, member1, Role.MEMBER)).getUuid();
        String department2memberRole3Uuid = transactionTemplate.execute(status -> userRoleService.findByResourceAndUserAndRole(department2, member3, Role.MEMBER)).getUuid();

        String parentRedirect2 = serverUrl + "/redirect?resource=" + boardId2;
        String resourceRedirect2 = serverUrl + "/redirect?resource=" + postR2.getId();
        User user2 = transactionTemplate.execute(status -> userCacheService.findOne(userId2));

        testNotificationService.stop();
        testNotificationService.verify(
            new TestNotificationService.NotificationInstance(Notification.PUBLISH_POST_NOTIFICATION, user2,
                ImmutableMap.<String, String>builder().put("recipient", user2.getGivenName()).put("department", departmentName2).put("board", boardName2)
                    .put("post", postName2).put("resourceRedirect", resourceRedirect2).put("modal", "Login").build()),
            new TestNotificationService.NotificationInstance(Notification.PUBLISH_POST_MEMBER_NOTIFICATION, member1,
                ImmutableMap.<String, String>builder().put("recipient", "member1").put("department", departmentName2).put("board", boardName2).put("post", postName2)
                    .put("organization", "organization name").put("summary", "summary").put("resourceRedirect", resourceRedirect2)
                    .put("invitationUuid", department2memberRole1Uuid).put("modal", "Login").put("parentRedirect", parentRedirect2)
                    .put("recipientUuid", member1.getUuid()).build()),
            new TestNotificationService.NotificationInstance(Notification.PUBLISH_POST_MEMBER_NOTIFICATION, member3,
                ImmutableMap.<String, String>builder().put("recipient", "member3").put("department", departmentName2).put("board", boardName2).put("post", postName2)
                    .put("organization", "organization name").put("summary", "summary").put("resourceRedirect", resourceRedirect2)
                    .put("invitationUuid", department2memberRole3Uuid).put("modal", "Register").put("parentRedirect", parentRedirect2)
                    .put("recipientUuid", member3.getUuid()).build()));

        Long postId2 = postR2.getId();
        transactionTemplate.execute(status -> authenticationApi.login(
            new LoginDTO().setUuid(department2memberRole1Uuid).setEmail("member1@member1.com").setPassword("password1")));
        testUserService.setAuthentication(member1.getId());
        departmentApi.putMembershipUpdate(departmentId2, new UserRoleDTO().setUser(new UserDTO().setGender(Gender.FEMALE).setAgeRange(AgeRange.TWENTYFIVE_TWENTYNINE)
            .setLocationNationality(new LocationDTO().setName("United Kingdom").setDomicile("GBR").setGoogleId("googleId").setLatitude(BigDecimal.ONE).setLongitude(BigDecimal.ONE))));

        postR2 = transactionTemplate.execute(status -> postApi.getPost(postId2, TestHelper.mockHttpServletRequest("ip1")));
        Assert.assertNotNull(postR2.getReferral());

        transactionTemplate.execute(status -> authenticationApi.login(
            new LoginDTO().setUuid(department2memberRole3Uuid).setEmail("member4@member4.com").setPassword("password4")));
        testUserService.setAuthentication(member2.getId());
        departmentApi.putMembershipUpdate(departmentId2, new UserRoleDTO().setUser(new UserDTO().setGender(Gender.FEMALE).setAgeRange(AgeRange.TWENTYFIVE_TWENTYNINE)
            .setLocationNationality(new LocationDTO().setName("United Kingdom").setDomicile("GBR").setGoogleId("googleId").setLatitude(BigDecimal.ONE).setLongitude(BigDecimal.ONE))));

        postR2 = transactionTemplate.execute(status -> postApi.getPost(postId2, TestHelper.mockHttpServletRequest("ip1")));
        Assert.assertNotNull(postR2.getReferral());

        testUserService.setAuthentication(userId2);
        List<String> emails2 = resourceApi.getUserRoles(Scope.DEPARTMENT, departmentId2, null)
            .getMembers().stream().map(userRole -> userRole.getUser().getEmail()).collect(Collectors.toList());
        verifyContains(emails2, BoardUtils.obfuscateEmail("member1@member1.com"), BoardUtils.obfuscateEmail("member4@member4.com"));

        Long userId3 = testUserService.authenticate().getId();
        BoardRepresentation boardR3 = transactionTemplate.execute(status -> boardApi.postBoard(
            new BoardDTO()
                .setName("board3")
                .setDepartment(new DepartmentDTO()
                    .setName("department3"))));

        DepartmentRepresentation departmentR3 = boardR3.getDepartment();
        Long departmentId3 = departmentR3.getId();
        Long boardId3 = boardR3.getId();

        transactionTemplate.execute(status -> departmentApi.postMembers(departmentId3, Arrays.asList(
            new UserRoleDTO().setUser(new UserDTO().setGivenName("member5").setSurname("member5").setEmail("member5@member5.com")).setRole(Role.MEMBER),
            new UserRoleDTO().setUser(new UserDTO().setGivenName("member6").setSurname("member6").setEmail("member6@member6.com")).setRole(Role.MEMBER),
            new UserRoleDTO().setUser(new UserDTO().setGivenName("member7").setSurname("member7").setEmail("member7@member7.com")).setRole(Role.MEMBER))));

        testNotificationService.record();
        PostRepresentation postR3 = transactionTemplate.execute(status -> postApi.postPost(boardId3, TestHelper.smallSamplePost()));
        transactionTemplate.execute(status -> {
            postService.publishAndRetirePosts();
            return null;
        });

        String postName3 = postR3.getName();
        String boardName3 = boardR3.getName();
        String departmentName3 = departmentR3.getName();

        User member5 = transactionTemplate.execute(status -> userRepository.findByEmail("member5@member5.com"));
        User member6 = transactionTemplate.execute(status -> userRepository.findByEmail("member6@member6.com"));
        User member7 = transactionTemplate.execute(status -> userRepository.findByEmail("member7@member7.com"));

        Resource department3 = transactionTemplate.execute(status -> resourceService.findOne(departmentId3));
        String department3memberRole1Uuid = transactionTemplate.execute(status -> userRoleService.findByResourceAndUserAndRole(department3, member5, Role.MEMBER)).getUuid();
        String department3memberRole2Uuid = transactionTemplate.execute(status -> userRoleService.findByResourceAndUserAndRole(department3, member6, Role.MEMBER)).getUuid();
        String department3memberRole3Uuid = transactionTemplate.execute(status -> userRoleService.findByResourceAndUserAndRole(department3, member7, Role.MEMBER)).getUuid();

        String parentRedirect3 = serverUrl + "/redirect?resource=" + boardId3;
        String resourceRedirect3 = serverUrl + "/redirect?resource=" + postR3.getId();
        User user3 = transactionTemplate.execute(status -> userCacheService.findOne(userId3));

        testNotificationService.stop();
        testNotificationService.verify(
            new TestNotificationService.NotificationInstance(Notification.PUBLISH_POST_NOTIFICATION, user3,
                ImmutableMap.<String, String>builder().put("recipient", user3.getGivenName()).put("department", departmentName3).put("board", boardName3)
                    .put("post", postName3).put("resourceRedirect", resourceRedirect3).put("modal", "Login").build()),
            new TestNotificationService.NotificationInstance(Notification.PUBLISH_POST_MEMBER_NOTIFICATION, member5,
                ImmutableMap.<String, String>builder().put("recipient", "member5").put("department", departmentName3).put("board", boardName3).put("post", postName3)
                    .put("organization", "organization name").put("summary", "summary").put("resourceRedirect", resourceRedirect3)
                    .put("invitationUuid", department3memberRole1Uuid).put("modal", "Register").put("parentRedirect", parentRedirect3)
                    .put("recipientUuid", member5.getUuid()).build()),
            new TestNotificationService.NotificationInstance(Notification.PUBLISH_POST_MEMBER_NOTIFICATION, member6,
                ImmutableMap.<String, String>builder().put("recipient", "member6").put("department", departmentName3).put("board", boardName3).put("post", postName3)
                    .put("organization", "organization name").put("summary", "summary").put("resourceRedirect", resourceRedirect3)
                    .put("invitationUuid", department3memberRole2Uuid).put("modal", "Register").put("parentRedirect", parentRedirect3)
                    .put("recipientUuid", member6.getUuid()).build()),
            new TestNotificationService.NotificationInstance(Notification.PUBLISH_POST_MEMBER_NOTIFICATION, member7,
                ImmutableMap.<String, String>builder().put("recipient", "member7").put("department", departmentName3).put("board", boardName3).put("post", postName3)
                    .put("organization", "organization name").put("summary", "summary").put("resourceRedirect", resourceRedirect3)
                    .put("invitationUuid", department3memberRole3Uuid).put("modal", "Register").put("parentRedirect", parentRedirect3)
                    .put("recipientUuid", member7.getUuid()).build()));

        Long postId3 = postR3.getId();
        transactionTemplate.execute(status -> authenticationApi.signin("facebook",
            new SigninDTO().setUuid(department3memberRole1Uuid).setClientId("clientId").setCode("code").setRedirectUri("redirectUri")));
        testUserService.setAuthentication(member5.getId());
        departmentApi.putMembershipUpdate(departmentId3, new UserRoleDTO().setUser(new UserDTO().setGender(Gender.FEMALE).setAgeRange(AgeRange.TWENTYFIVE_TWENTYNINE)
            .setLocationNationality(new LocationDTO().setName("United Kingdom").setDomicile("GBR").setGoogleId("googleId").setLatitude(BigDecimal.ONE).setLongitude(BigDecimal.ONE))));

        postR3 = transactionTemplate.execute(status -> postApi.getPost(postId3, TestHelper.mockHttpServletRequest("ip5")));
        Assert.assertNotNull(postR3.getReferral());

        transactionTemplate.execute(status -> authenticationApi.signin("facebook",
            new SigninDTO().setUuid(department3memberRole2Uuid).setClientId("clientId2").setCode("code2").setRedirectUri("redirectUri2")));
        testUserService.setAuthentication(member6.getId());
        departmentApi.putMembershipUpdate(departmentId3, new UserRoleDTO().setUser(new UserDTO().setGender(Gender.FEMALE).setAgeRange(AgeRange.TWENTYFIVE_TWENTYNINE)
            .setLocationNationality(new LocationDTO().setName("United Kingdom").setDomicile("GBR").setGoogleId("googleId").setLatitude(BigDecimal.ONE).setLongitude(BigDecimal.ONE))));

        postR3 = transactionTemplate.execute(status -> postApi.getPost(postId3, TestHelper.mockHttpServletRequest("ip6")));
        Assert.assertNotNull(postR3.getReferral());

        transactionTemplate.execute(status -> authenticationApi.signin("facebook",
            new SigninDTO().setUuid(department3memberRole3Uuid).setClientId("clientId3").setCode("code3").setRedirectUri("redirectUri3")));
        testUserService.setAuthentication(member1.getId());
        departmentApi.putMembershipUpdate(departmentId3, new UserRoleDTO().setUser(new UserDTO().setGender(Gender.FEMALE).setAgeRange(AgeRange.TWENTYFIVE_TWENTYNINE)
            .setLocationNationality(new LocationDTO().setName("United Kingdom").setDomicile("GBR").setGoogleId("googleId").setLatitude(BigDecimal.ONE).setLongitude(BigDecimal.ONE))));

        postR3 = transactionTemplate.execute(status -> postApi.getPost(postId3, TestHelper.mockHttpServletRequest("ip7")));
        Assert.assertNotNull(postR3.getReferral());

        testUserService.setAuthentication(userId3);
        List<String> emails3 = resourceApi.getUserRoles(Scope.DEPARTMENT, departmentId3, null)
            .getMembers().stream().map(userRole -> userRole.getUser().getEmail()).collect(Collectors.toList());
        verifyContains(emails3, BoardUtils.obfuscateEmail("alastair@prism.hr"),
            BoardUtils.obfuscateEmail("jakub@prism.hr"), BoardUtils.obfuscateEmail("member1@member1.com"));

        Long userId4 = testUserService.authenticate().getId();
        BoardRepresentation boardR4 = transactionTemplate.execute(status -> boardApi.postBoard(
            new BoardDTO()
                .setName("board4")
                .setDepartment(new DepartmentDTO()
                    .setName("department4"))));

        DepartmentRepresentation departmentR4 = boardR4.getDepartment();
        Long departmentId4 = departmentR4.getId();
        Long boardId4 = boardR4.getId();

        transactionTemplate.execute(status -> departmentApi.postMembers(departmentId4, Collections.singletonList(
            new UserRoleDTO().setUser(new UserDTO().setGivenName("member8").setSurname("member8").setEmail("member8@member8.com")).setRole(Role.MEMBER))));

        testNotificationService.record();
        PostRepresentation postR4 = transactionTemplate.execute(status -> postApi.postPost(boardId4, TestHelper.smallSamplePost()));
        transactionTemplate.execute(status -> {
            postService.publishAndRetirePosts();
            return null;
        });

        String postName4 = postR4.getName();
        String boardName4 = boardR4.getName();
        String departmentName4 = departmentR4.getName();

        User member8 = transactionTemplate.execute(status -> userRepository.findByEmail("member8@member8.com"));

        Resource department4 = transactionTemplate.execute(status -> resourceService.findOne(departmentId4));
        String department4memberRole1Uuid = transactionTemplate.execute(status -> userRoleService.findByResourceAndUserAndRole(department4, member8, Role.MEMBER)).getUuid();

        String parentRedirect4 = serverUrl + "/redirect?resource=" + boardId4;
        String resourceRedirect4 = serverUrl + "/redirect?resource=" + postR4.getId();
        User user4 = transactionTemplate.execute(status -> userCacheService.findOne(userId4));

        testNotificationService.stop();
        testNotificationService.verify(
            new TestNotificationService.NotificationInstance(Notification.PUBLISH_POST_NOTIFICATION, user4,
                ImmutableMap.<String, String>builder().put("recipient", user4.getGivenName()).put("department", departmentName4).put("board", boardName4)
                    .put("post", postName4).put("resourceRedirect", resourceRedirect4).put("modal", "Login").build()),
            new TestNotificationService.NotificationInstance(Notification.PUBLISH_POST_MEMBER_NOTIFICATION, member8,
                ImmutableMap.<String, String>builder().put("recipient", "member8").put("department", departmentName4).put("board", boardName4).put("post", postName4)
                    .put("organization", "organization name").put("summary", "summary").put("resourceRedirect", resourceRedirect4)
                    .put("invitationUuid", department4memberRole1Uuid).put("modal", "Register").put("parentRedirect", parentRedirect4)
                    .put("recipientUuid", member8.getUuid()).build()));

        Long postId4 = postR4.getId();
        transactionTemplate.execute(status -> authenticationApi.signin("facebook",
            new SigninDTO().setUuid(department4memberRole1Uuid).setClientId("clientId").setCode("code").setRedirectUri("redirectUri")));
        testUserService.setAuthentication(member5.getId());
        departmentApi.putMembershipUpdate(departmentId4, new UserRoleDTO().setUser(new UserDTO().setGender(Gender.FEMALE).setAgeRange(AgeRange.TWENTYFIVE_TWENTYNINE)
            .setLocationNationality(new LocationDTO().setName("United Kingdom").setDomicile("GBR").setGoogleId("googleId").setLatitude(BigDecimal.ONE).setLongitude(BigDecimal.ONE))));

        postR4 = transactionTemplate.execute(status -> postApi.getPost(postId4, TestHelper.mockHttpServletRequest("ip5")));
        Assert.assertNotNull(postR4.getReferral());

        testUserService.setAuthentication(userId4);
        List<String> emails4 = resourceApi.getUserRoles(Scope.DEPARTMENT, departmentId4, null)
            .getMembers().stream().map(userRole -> userRole.getUser().getEmail()).collect(Collectors.toList());
        verifyContains(emails4, BoardUtils.obfuscateEmail("alastair@prism.hr"));
    }

    private void verifyAccessToken(String loginAccessToken, Long userId) {
        Assert.assertNotNull(loginAccessToken);
        Assert.assertEquals(userId,
            new Long(Long.parseLong(Jwts.parser().setSigningKey(authenticationService.getJwsSecret()).parseClaimsJws(loginAccessToken).getBody().getSubject())));
    }

}
