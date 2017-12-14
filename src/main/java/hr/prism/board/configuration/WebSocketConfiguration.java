package hr.prism.board.configuration;

import com.pusher.rest.Pusher;
import hr.prism.board.authentication.AuthenticationChannelAdapter;
import hr.prism.board.authentication.AuthorizationHeaderResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.support.ChannelInterceptorAdapter;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

import javax.inject.Inject;

@Configuration
@EnableWebSocketMessageBroker
@SuppressWarnings("SpringAutowiredFieldsWarningInspection")
public class WebSocketConfiguration extends AbstractWebSocketMessageBrokerConfigurer {

    @Value("${pusher.app}")
    private String pusherApp;

    @Value("${pusher.key}")
    private String pusherKey;

    @Value("${pusher.secret}")
    private String pusherSecret;

    @Value("${pusher.cluster}")
    private String pusherCluster;

    @Inject
    private AuthorizationHeaderResolver authorizationHeaderResolver;

    @Bean
    public Pusher pusher() {
        Pusher pusher = new Pusher(pusherApp, pusherKey, pusherSecret);
        pusher.setCluster(pusherCluster);
        return pusher;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/activities");
        registry.setUserDestinationPrefix("/api/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/api/web-socket").setAllowedOrigins("*").withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new AuthenticationChannelAdapter(authorizationHeaderResolver));
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ConnectAckDeferAdapter());
    }

    private class ConnectAckDeferAdapter extends ChannelInterceptorAdapter {
        @Override
        public Message<?> preSend(Message<?> message, MessageChannel channel) {
            if (message.getHeaders().get("simpMessageType").equals(SimpMessageType.CONNECT_ACK)) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    throw new Error(e);
                }
            }

            return message;
        }

    }

}

