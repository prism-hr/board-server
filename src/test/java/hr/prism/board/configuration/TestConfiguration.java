package hr.prism.board.configuration;

import hr.prism.board.authentication.adapter.FacebookAdapter;
import hr.prism.board.authentication.adapter.LinkedinAdapter;
import hr.prism.board.domain.User;
import hr.prism.board.dto.OAuthAuthorizationDataDTO;
import hr.prism.board.dto.OAuthDataDTO;
import hr.prism.board.dto.SigninDTO;
import hr.prism.board.event.ActivityEvent;
import hr.prism.board.event.NotificationEvent;
import hr.prism.board.event.UserRoleEvent;
import hr.prism.board.event.consumer.ActivityEventConsumer;
import hr.prism.board.event.consumer.NotificationEventConsumer;
import hr.prism.board.event.consumer.UserRoleEventConsumer;
import hr.prism.board.service.TestActivityService;
import hr.prism.board.service.TestNotificationService;
import hr.prism.board.service.TestPaymentService;
import hr.prism.board.service.TestScheduledService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.inject.Inject;

import static hr.prism.board.enums.OauthProvider.FACEBOOK;
import static hr.prism.board.enums.OauthProvider.LINKEDIN;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@Configuration
public class TestConfiguration {

    private final ApplicationContext applicationContext;

    @Inject
    public TestConfiguration(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Bean
    @Primary
    public FacebookAdapter facebookAdapter() {
        FacebookAdapter facebookAdapter = mock(FacebookAdapter.class);
        when(facebookAdapter.exchangeForUser(
            new SigninDTO()
                .setAuthorizationData(
                    new OAuthAuthorizationDataDTO()
                        .setClientId("clientId")
                        .setRedirectUri("redirectUri"))
                .setOauthData(
                    new OAuthDataDTO()
                        .setCode("code"))))
            .thenReturn(
                new User()
                    .setGivenName("alastair")
                    .setSurname("knowles")
                    .setEmail("alastair@prism.hr")
                    .setOauthProvider(FACEBOOK)
                    .setOauthAccountId("facebookId"));

        when(facebookAdapter.exchangeForUser(
            new SigninDTO()
                .setAuthorizationData(
                    new OAuthAuthorizationDataDTO()
                        .setClientId("clientId2")
                        .setRedirectUri("redirectUri2"))
                .setOauthData(
                    new OAuthDataDTO()
                        .setCode("code2"))))
            .thenReturn(
                new User()
                    .setGivenName("jakub")
                    .setSurname("fibinger")
                    .setEmail("jakub@prism.hr")
                    .setOauthProvider(FACEBOOK)
                    .setOauthAccountId("facebookId2"));

        when(facebookAdapter.exchangeForUser(
            new SigninDTO()
                .setAuthorizationData(
                    new OAuthAuthorizationDataDTO()
                        .setClientId("clientId3")
                        .setRedirectUri("redirectUri3"))
                .setOauthData(
                    new OAuthDataDTO()
                        .setCode("code3"))))
            .thenReturn(
                new User()
                    .setGivenName("member1")
                    .setSurname("member1")
                    .setEmail("member1@member1.com")
                    .setOauthProvider(FACEBOOK)
                    .setOauthAccountId("facebookId3"));

        return facebookAdapter;
    }

    @Bean
    @Primary
    public LinkedinAdapter linkedinAdapter() {
        LinkedinAdapter linkedinAdapter = mock(LinkedinAdapter.class);
        when(linkedinAdapter.exchangeForUser(
            new SigninDTO()
                .setAuthorizationData(
                    new OAuthAuthorizationDataDTO()
                        .setClientId("clientId")
                        .setRedirectUri("redirectUri"))
                .setOauthData(
                    new OAuthDataDTO()
                        .setCode("code"))))
            .thenReturn(
                new User()
                    .setGivenName("alastair")
                    .setSurname("knowles")
                    .setEmail("alastair@prism.hr")
                    .setOauthProvider(LINKEDIN)
                    .setOauthAccountId("linkedinId"));

        return linkedinAdapter;
    }

    @Bean
    @Primary
    public TestNotificationService notificationService() {
        return new TestNotificationService();
    }

    @Bean
    @Primary
    public TestActivityService activityService() {
        return new TestActivityService();
    }

    @Bean
    @Primary
    public TestScheduledService scheduledService() {
        return new TestScheduledService();
    }

    @Bean
    @Primary
    public TestPaymentService testPaymentService() {
        return new TestPaymentService();
    }

    @Bean
    @Primary
    public ApplicationEventPublisher applicationEventPublisher() {
        ApplicationEventPublisher applicationEventPublisher = mock(ApplicationEventPublisher.class);

        ActivityEventConsumer activityEventConsumer = applicationContext.getBean(ActivityEventConsumer.class);
        NotificationEventConsumer notificationEventConsumer =
            applicationContext.getBean(NotificationEventConsumer.class);
        UserRoleEventConsumer userRoleEventConsumer = applicationContext.getBean(UserRoleEventConsumer.class);

        doAnswer(invocation -> {
            ApplicationEvent event = (ApplicationEvent) invocation.getArguments()[0];
            if (event instanceof ActivityEvent) {
                activityEventConsumer.consume((ActivityEvent) invocation.getArguments()[0]);
            } else if (event instanceof NotificationEvent) {
                notificationEventConsumer.consume((NotificationEvent) invocation.getArguments()[0]);
            } else if (event instanceof UserRoleEvent) {
                userRoleEventConsumer.consume((UserRoleEvent) invocation.getArguments()[0]);
            }

            return event;
        }).when(applicationEventPublisher)
            .publishEvent(any(ApplicationEvent.class));

        return applicationEventPublisher;
    }

}
