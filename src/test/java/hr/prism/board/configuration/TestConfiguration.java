package hr.prism.board.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pusher.rest.Pusher;
import hr.prism.board.authentication.adapter.FacebookAdapter;
import hr.prism.board.authentication.adapter.LinkedinAdapter;
import hr.prism.board.dao.ActivityDAO;
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
import hr.prism.board.mapper.ActivityMapper;
import hr.prism.board.repository.ActivityEventRepository;
import hr.prism.board.repository.ActivityRepository;
import hr.prism.board.repository.ActivityRoleRepository;
import hr.prism.board.repository.ActivityUserRepository;
import hr.prism.board.service.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import static hr.prism.board.enums.OauthProvider.FACEBOOK;
import static hr.prism.board.enums.OauthProvider.LINKEDIN;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@Configuration
public class TestConfiguration {

    private final boolean pusherOn;

    private final ActivityRepository activityRepository;

    private final ActivityDAO activityDAO;

    private final ActivityRoleRepository activityRoleRepository;

    private final ActivityUserRepository activityUserRepository;

    private final ActivityEventRepository activityEventRepository;

    private final UserService userService;

    private final ActivityMapper activityMapper;

    private final Pusher pusher;

    private final ObjectMapper objectMapper;

    private final EntityManager entityManager;

    private final ApplicationContext applicationContext;

    @Inject
    public TestConfiguration(@Value("${pusher.on}") boolean pusherOn, ActivityRepository activityRepository,
                             ActivityDAO activityDAO, ActivityRoleRepository activityRoleRepository,
                             ActivityUserRepository activityUserRepository,
                             ActivityEventRepository activityEventRepository, UserService userService,
                             ActivityMapper activityMapper, Pusher pusher, ObjectMapper objectMapper,
                             EntityManager entityManager, ApplicationContext applicationContext) {
        this.pusherOn = pusherOn;
        this.activityRepository = activityRepository;
        this.activityDAO = activityDAO;
        this.activityRoleRepository = activityRoleRepository;
        this.activityUserRepository = activityUserRepository;
        this.activityEventRepository = activityEventRepository;
        this.userService = userService;
        this.activityMapper = activityMapper;
        this.pusher = pusher;
        this.objectMapper = objectMapper;
        this.entityManager = entityManager;
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
        return new TestActivityService(pusherOn, activityRepository, activityDAO, activityRoleRepository,
            activityUserRepository, activityEventRepository, userService, activityMapper, pusher, objectMapper,
            entityManager);
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
