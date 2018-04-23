package hr.prism.board.configuration;

import com.google.gson.*;
import com.pusher.rest.Pusher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import javax.inject.Inject;
import java.lang.reflect.Type;
import java.time.LocalDateTime;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;

public class PusherConfiguration {

    private final String pusherApp;

    private final String pusherKey;

    private final String pusherSecret;

    private final String pusherCluster;

    @Inject
    public PusherConfiguration(@Value("${pusher.app}") String pusherApp, @Value("${pusher.key}") String pusherKey,
                               @Value("${pusher.secret}") String pusherSecret,
                               @Value("${pusher.cluster}") String pusherCluster) {
        this.pusherApp = pusherApp;
        this.pusherKey = pusherKey;
        this.pusherSecret = pusherSecret;
        this.pusherCluster = pusherCluster;
    }

    @Bean
    public Pusher pusher() {
        Pusher pusher = new Pusher(pusherApp, pusherKey, pusherSecret);
        pusher.setCluster(pusherCluster);
        pusher.setGsonSerialiser(
            new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new GsonLocalDateTimeAdapter())
                .create());
        return pusher;
    }

    private static class GsonLocalDateTimeAdapter implements JsonSerializer<LocalDateTime> {

        public JsonElement serialize(LocalDateTime localDateTime, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(localDateTime.format(ISO_LOCAL_DATE_TIME));
        }

    }

}
