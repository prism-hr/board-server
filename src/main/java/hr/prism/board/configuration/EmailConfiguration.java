package hr.prism.board.configuration;

import com.sendgrid.SendGrid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.inject.Inject;

@Configuration
@Profile({"local", "uat", "prod"})
public class EmailConfiguration {

    private final String sendgridKey;

    @Inject
    public EmailConfiguration(@Value("${sendgrid.key}") String sendgridKey) {
        this.sendgridKey = sendgridKey;
    }

    @Bean
    public SendGrid sendGrid() {
        return new SendGrid(sendgridKey);
    }

}
