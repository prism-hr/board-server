package hr.prism.board.configuration;

import com.sendgrid.SendGrid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.inject.Inject;

@Configuration
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
