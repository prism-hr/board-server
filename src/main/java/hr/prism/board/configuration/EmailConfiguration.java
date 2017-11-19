package hr.prism.board.configuration;

import com.sendgrid.SendGrid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmailConfiguration {


    @Value("${sendgrid.key}")
    private String sendgridKey;

    @Bean
    public SendGrid sendGrid() {
        return new SendGrid(sendgridKey);
    }

}
