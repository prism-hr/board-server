package hr.prism.board.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"local", "uat", "prod"})
@Import({AsyncConfiguration.class, DatabaseConfiguration.class, EmailConfiguration.class,
    FreeMarkerConfiguration.class, JacksonConfiguration.class, PusherConfiguration.class,
    SchedulingConfiguration.class, SecurityConfiguration.class, WebMvcConfiguration.class})
public class AppConfiguration {

}
