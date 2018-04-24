package hr.prism.board.configuration;

import com.sendgrid.SendGrid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import javax.inject.Inject;

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
