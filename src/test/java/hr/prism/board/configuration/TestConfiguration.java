package hr.prism.board.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pusher.rest.Pusher;
import com.sendgrid.SendGrid;
import com.stripe.model.Customer;
import com.stripe.model.InvoiceCollection;
import hr.prism.board.authentication.adapter.FacebookAdapter;
import hr.prism.board.authentication.adapter.LinkedinAdapter;
import hr.prism.board.dao.ActivityDAO;
import hr.prism.board.domain.User;
import hr.prism.board.dto.OAuthAuthorizationDataDTO;
import hr.prism.board.dto.OAuthDataDTO;
import hr.prism.board.dto.SigninDTO;
import hr.prism.board.event.ActivityEvent;
import hr.prism.board.event.DepartmentMemberEvent;
import hr.prism.board.event.NotificationEvent;
import hr.prism.board.event.consumer.ActivityEventConsumer;
import hr.prism.board.event.consumer.DepartmentMemberEventConsumer;
import hr.prism.board.event.consumer.NotificationEventConsumer;
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

import javax.persistence.EntityManager;

import static hr.prism.board.enums.OauthProvider.FACEBOOK;
import static hr.prism.board.enums.OauthProvider.LINKEDIN;
import static org.mockito.Mockito.*;

@Configuration
public class TestConfiguration {

    private final ActivityService activityService;

    private final NotificationService notificationService;

    private final ApplicationContext applicationContext;

    public TestConfiguration(@Value("${pusher.on}") boolean pusherOn, @Value("${mail.on}") boolean mailOn,
                             @Value("${system.email}") String senderEmail, ActivityRepository activityRepository,
                             ActivityDAO activityDAO, ActivityRoleRepository activityRoleRepository,
                             ActivityUserRepository activityUserRepository,
                             ActivityEventRepository activityEventRepository, UserService userService,
                             ActivityMapper activityMapper, TestEmailService testEmailService, Pusher pusher,
                             SendGrid sendGrid, ObjectMapper objectMapper, EntityManager entityManager,
                             ApplicationContext applicationContext) {
        this.activityService = new TestActivityService(pusherOn, activityRepository, activityDAO,
            activityRoleRepository, activityUserRepository, activityEventRepository, userService,
            activityMapper, pusher, objectMapper, entityManager);

        this.notificationService = new TestNotificationService(mailOn, senderEmail, testEmailService, sendGrid,
            applicationContext);

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
    public ActivityService activityService() {
        return this.activityService;
    }

    @Bean
    @Primary
    public NotificationService notificationService() {
        return this.notificationService;
    }

//    @Bean
//    @Primary
//    public SendGrid sendGrid() {
//        return mock(SendGrid.class);
//    }
//
//    @Bean
//    @Primary
//    public Pusher pusher() {
//        return mock(Pusher.class);
//    }

    @Bean
    @Primary
    public PaymentService paymentService() {
        PaymentService paymentService = mock(PaymentService.class);
        Customer customer = new Customer();
        customer.setId("id");

        when(paymentService.getCustomer(anyString())).thenReturn(customer);
        when(paymentService.createCustomer(anyString())).thenReturn(customer);
        when(paymentService.createSubscription(anyString())).thenReturn(customer);
        when(paymentService.appendSource(anyString(), anyString())).thenReturn(customer);
        when(paymentService.setSourceAsDefault(anyString(), anyString())).thenReturn(customer);
        when(paymentService.deleteSource(anyString(), anyString())).thenReturn(customer);
        when(paymentService.cancelSubscription(anyString())).thenReturn(customer);
        when(paymentService.reactivateSubscription(anyString())).thenReturn(customer);

        InvoiceCollection invoiceCollection = new InvoiceCollection();
        when(paymentService.getInvoices(anyString())).thenReturn(invoiceCollection);

        return paymentService;
    }

    @Bean
    @Primary
    public ApplicationEventPublisher applicationEventPublisher() {
        ApplicationEventPublisher applicationEventPublisher = mock(ApplicationEventPublisher.class);

        ActivityEventConsumer activityEventConsumer =
            applicationContext.getBean(ActivityEventConsumer.class);
        NotificationEventConsumer notificationEventConsumer =
            applicationContext.getBean(NotificationEventConsumer.class);
        DepartmentMemberEventConsumer departmentMemberEventConsumer =
            applicationContext.getBean(DepartmentMemberEventConsumer.class);

        doAnswer(invocation -> {
            ApplicationEvent event = (ApplicationEvent) invocation.getArguments()[0];
            if (event instanceof ActivityEvent) {
                activityEventConsumer.consume((ActivityEvent) invocation.getArguments()[0]);
            } else if (event instanceof NotificationEvent) {
                notificationEventConsumer.consume((NotificationEvent) invocation.getArguments()[0]);
            } else if (event instanceof DepartmentMemberEvent) {
                departmentMemberEventConsumer.consume((DepartmentMemberEvent) invocation.getArguments()[0]);
            }

            return event;
        }).when(applicationEventPublisher)
            .publishEvent(any(ApplicationEvent.class));

        return applicationEventPublisher;
    }

}
