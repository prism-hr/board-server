package hr.prism.board.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

import javax.inject.Inject;

import hr.prism.board.authentication.AuthenticationChannelAdapter;
import hr.prism.board.service.AuthenticationService;

@Configuration
@EnableWebSocketMessageBroker
@SuppressWarnings("SpringAutowiredFieldsWarningInspection")
public class WebSocketConfiguration extends AbstractWebSocketMessageBrokerConfigurer {

    @Inject
    private AuthenticationService authenticationService;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setUserDestinationPrefix("/api/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/web-socket").withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new AuthenticationChannelAdapter(authenticationService));
    }

}
