package hr.prism.board.configuration;

import com.google.gson.*;
import com.pusher.rest.Pusher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
        pusher.setGsonSerialiser(
            new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new GsonLocalDateTimeAdapter())
                .create());
        return pusher;
    }

    private static class GsonLocalDateTimeAdapter implements JsonSerializer<LocalDateTime> {

        public JsonElement serialize(LocalDateTime localDateTime, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }

    }

}
