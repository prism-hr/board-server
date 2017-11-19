package hr.prism.board.authentication;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptorAdapter;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;

import hr.prism.board.service.AuthenticationService;
import hr.prism.board.utils.BoardUtils;

public class AuthenticationChannelAdapter extends ChannelInterceptorAdapter {

    private AuthenticationService authenticationService;

    public AuthenticationChannelAdapter(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authorization = (String) accessor.getHeader("Authorization");
            Authentication authenticationToken = BoardUtils.makeAuthenticationToken(authenticationService, authorization);
            accessor.setUser(authenticationToken);
        }

        return message;
    }

}
