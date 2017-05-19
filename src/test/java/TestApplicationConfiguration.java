import hr.prism.board.ApplicationConfiguration;
import hr.prism.board.SecurityConfiguration;
import hr.prism.board.repository.MyRepositoryImpl;
import hr.prism.board.service.TestNotificationService;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@EnableWebMvc
@Configuration
@EnableScheduling
@SpringBootApplication
@EnableJpaRepositories(repositoryBaseClass = MyRepositoryImpl.class)
@Import({ApplicationConfiguration.class, SecurityConfiguration.class})
public class TestApplicationConfiguration {
    
    @Bean
    @Primary
    public TestNotificationService notificationService() {
        return new TestNotificationService();
    }
    
}
