package hr.prism.board.configuration;

import hr.prism.board.authentication.adapter.FacebookAdapter;
import hr.prism.board.authentication.adapter.LinkedinAdapter;
import hr.prism.board.domain.User;
import hr.prism.board.dto.OAuthAuthorizationDataDTO;
import hr.prism.board.dto.OAuthDataDTO;
import hr.prism.board.dto.SigninDTO;
import hr.prism.board.enums.OauthProvider;
import hr.prism.board.service.TestActivityService;
import hr.prism.board.service.TestDepartmentScheduledService;
import hr.prism.board.service.TestNotificationService;
import hr.prism.board.service.event.*;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class TestConfiguration {

    @Bean
    @Primary
    public FacebookAdapter facebookAdapter() {
        FacebookAdapter facebookAdapter = Mockito.mock(FacebookAdapter.class);
        Mockito.when(facebookAdapter.exchangeForUser(
            new SigninDTO()
                .setAuthorizationData(new OAuthAuthorizationDataDTO().setClientId("clientId").setRedirectUri("redirectUri"))
                .setOauthData(new OAuthDataDTO().setCode("code"))))
            .thenReturn(
                new User()
                    .setGivenName("alastair")
                    .setSurname("knowles")
                    .setEmail("alastair@prism.hr")
                    .setOauthProvider(OauthProvider.FACEBOOK)
                    .setOauthAccountId("facebookId"));

        Mockito.when(facebookAdapter.exchangeForUser(
            new SigninDTO()
                .setAuthorizationData(new OAuthAuthorizationDataDTO().setClientId("clientId2").setRedirectUri("redirectUri2"))
                .setOauthData(new OAuthDataDTO().setCode("code2"))))
            .thenReturn(
                new User()
                    .setGivenName("jakub")
                    .setSurname("fibinger")
                    .setEmail("jakub@prism.hr")
                    .setOauthProvider(OauthProvider.FACEBOOK)
                    .setOauthAccountId("facebookId2"));

        Mockito.when(facebookAdapter.exchangeForUser(
            new SigninDTO()
                .setAuthorizationData(new OAuthAuthorizationDataDTO().setClientId("clientId3").setRedirectUri("redirectUri3"))
                .setOauthData(new OAuthDataDTO().setCode("code3"))))
            .thenReturn(
                new User()
                    .setGivenName("member1")
                    .setSurname("member1")
                    .setEmail("member1@member1.com")
                    .setOauthProvider(OauthProvider.FACEBOOK)
                    .setOauthAccountId("facebookId3"));

        return facebookAdapter;
    }

    @Bean
    @Primary
    public LinkedinAdapter linkedinAdapter() {
        LinkedinAdapter linkedinAdapter = Mockito.mock(LinkedinAdapter.class);
        Mockito.when(linkedinAdapter.exchangeForUser(
            new SigninDTO()
                .setAuthorizationData(new OAuthAuthorizationDataDTO().setClientId("clientId").setRedirectUri("redirectUri"))
                .setOauthData(new OAuthDataDTO().setCode("code"))))
            .thenReturn(
                new User()
                    .setGivenName("alastair")
                    .setSurname("knowles")
                    .setEmail("alastair@prism.hr")
                    .setOauthProvider(OauthProvider.LINKEDIN)
                    .setOauthAccountId("linkedinId"));

        return linkedinAdapter;
    }

    @Bean
    @Primary
    public NotificationEventService notificationEventService() {
        return new TestNotificationEventService();
    }

    @Bean
    @Primary
    public TestNotificationService notificationService() {
        return new TestNotificationService();
    }

    @Bean
    @Primary
    public UserRoleEventService userRoleEventService() {
        return new TestUserRoleEventService();
    }

    @Bean
    @Primary
    public TestActivityEventService activityEventService() {
        return new TestActivityEventService();
    }

    @Bean
    @Primary
    public TestActivityService userActivityService() {
        return new TestActivityService();
    }

    @Bean
    @Primary
    public TestDepartmentScheduledService departmentScheduledService() {
        return new TestDepartmentScheduledService();
    }

}
