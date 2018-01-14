package hr.prism.board.configuration;

import com.pusher.rest.Pusher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SuppressWarnings("SpringAutowiredFieldsWarningInspection")
public class WebSocketConfiguration {

    @Value("${pusher.app}")
    private String pusherApp;

    @Value("${pusher.key}")
    private String pusherKey;

    @Value("${pusher.secret}")
    private String pusherSecret;

    @Value("${pusher.cluster}")
    private String pusherCluster;

    @Bean
    public Pusher pusher() {
        Pusher pusher = new Pusher(pusherApp, pusherKey, pusherSecret);
        pusher.setCluster(pusherCluster);
        return pusher;
    }

}
