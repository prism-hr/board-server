package hr.prism.board;

import hr.prism.board.authentication.adapter.FacebookAdapter;
import hr.prism.board.authentication.adapter.LinkedinAdapter;
import hr.prism.board.domain.User;
import hr.prism.board.dto.OauthDTO;
import hr.prism.board.enums.OauthProvider;
import hr.prism.board.service.TestNotificationService;
import hr.prism.board.service.event.NotificationEventService;
import hr.prism.board.service.event.TestNotificationEventService;
import hr.prism.board.service.event.TestUserRoleEventService;
import hr.prism.board.service.event.UserRoleEventService;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

@Configuration
@Import(BoardApplication.class)
public class TestBoardApplication {

    @Bean
    @Primary
    public TestNotificationService notificationService() {
        return new TestNotificationService();
    }

    @Bean
    @Primary
    public FacebookAdapter facebookAdapter() {
        FacebookAdapter facebookAdapter = Mockito.mock(FacebookAdapter.class);
        Mockito.when(facebookAdapter.exchangeForUser(
            new OauthDTO()
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
        return facebookAdapter;
    }

    @Bean
    @Primary
    public LinkedinAdapter linkedinAdapter() {
        LinkedinAdapter linkedinAdapter = Mockito.mock(LinkedinAdapter.class);
        Mockito.when(linkedinAdapter.exchangeForUser(
            new OauthDTO()
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
    public UserRoleEventService userRoleEventService() {
        return new TestUserRoleEventService();
    }

}
