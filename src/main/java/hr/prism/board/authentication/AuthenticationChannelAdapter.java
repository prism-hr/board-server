package hr.prism.board.authentication;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptorAdapter;
import org.springframework.messaging.support.MessageHeaderAccessor;

public class AuthenticationChannelAdapter extends ChannelInterceptorAdapter {

    private AuthorizationHeaderResolver authorizationHeaderResolver;

    public AuthenticationChannelAdapter(AuthorizationHeaderResolver authorizationHeaderResolver) {
        this.authorizationHeaderResolver = authorizationHeaderResolver;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authorization = accessor.getNativeHeader("Authorization").get(0);
            Long userId = authorizationHeaderResolver.resolveUserId(authorization);
            accessor.setUser(new AuthenticationToken(userId));
        }

        return message;
    }

}
