package hr.prism.board.configuration;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import hr.prism.board.authentication.adapter.FacebookAdapter;
import hr.prism.board.authentication.adapter.LinkedinAdapter;
import hr.prism.board.domain.User;
import hr.prism.board.dto.SigninDTO;
import hr.prism.board.enums.OauthProvider;
import hr.prism.board.service.TestNotificationService;
import hr.prism.board.service.TestUserActivityService;
import hr.prism.board.service.event.NotificationEventService;
import hr.prism.board.service.event.TestActivityEventService;
import hr.prism.board.service.event.TestNotificationEventService;
import hr.prism.board.service.event.TestUserRoleEventService;
import hr.prism.board.service.event.UserRoleEventService;

@Configuration
public class TestConfiguration {

    @Bean
    @Primary
    public FacebookAdapter facebookAdapter() {
        FacebookAdapter facebookAdapter = Mockito.mock(FacebookAdapter.class);
        Mockito.when(facebookAdapter.exchangeForUser(
            new SigninDTO()
                .setClientId("clientId")
                .setCode("code")
                .setRedirectUri("redirectUri")))
            .thenReturn(
                new User()
                    .setGivenName("alastair")
                    .setSurname("knowles")
                    .setEmail("alastair@prism.hr")
                    .setOauthProvider(OauthProvider.FACEBOOK)
                    .setOauthAccountId("facebookId"));

        Mockito.when(facebookAdapter.exchangeForUser(
            new SigninDTO()
                .setClientId("clientId2")
                .setCode("code2")
                .setRedirectUri("redirectUri2")))
            .thenReturn(
                new User()
                    .setGivenName("jakub")
                    .setSurname("fibinger")
                    .setEmail("jakub@prism.hr")
                    .setOauthProvider(OauthProvider.FACEBOOK)
                    .setOauthAccountId("facebookId2"));

        Mockito.when(facebookAdapter.exchangeForUser(
            new SigninDTO()
                .setClientId("clientId3")
                .setCode("code3")
                .setRedirectUri("redirectUri3")))
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
                .setClientId("clientId")
                .setCode("code")
                .setRedirectUri("redirectUri")))
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
    public TestUserActivityService userActivityService() {
        return new TestUserActivityService();
    }

}
