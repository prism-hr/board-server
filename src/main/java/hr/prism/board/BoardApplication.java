package hr.prism.board;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.filter.ThresholdFilter;
import com.tapstream.rollbar.RollbarAppender;
import hr.prism.board.repository.MyRepositoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.io.InputStream;
import java.util.Properties;

@Configuration
@SpringBootApplication
@EnableJpaRepositories(repositoryBaseClass = MyRepositoryImpl.class)
public class BoardApplication extends WebMvcConfigurerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(BoardApplication.class);

    public static void main(String[] args) {
        ClassLoader classLoader = BoardApplication.class.getClassLoader();
        try (InputStream propertiesStream = classLoader.getResourceAsStream("application.properties")) {
            Properties properties = new Properties();
            properties.load(propertiesStream);

            String profile = properties.get("profile").toString();
            SpringApplication springApplication = new SpringApplication(BoardApplication.class);
            springApplication.setAdditionalProfiles(profile);
            springApplication.run(args);

            if ("uat".equals(profile) || "prod".equals(profile)) {
                LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
                RollbarAppender rollbarAppender = new RollbarAppender();
                rollbarAppender.setApiKey("f22ab8d8627945fcb587d757fc4e6a71");
                rollbarAppender.setEnvironment(profile);
                rollbarAppender.setContext(loggerContext);

                ThresholdFilter thresholdFilter = new ThresholdFilter();
                thresholdFilter.setLevel(Level.ERROR.levelStr);
                thresholdFilter.setContext(loggerContext);
                thresholdFilter.start();

                rollbarAppender.addFilter(thresholdFilter);
                ch.qos.logback.classic.Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
                rootLogger.addAppender(rollbarAppender);
                rollbarAppender.start();
            }
        } catch (Exception e) {
            LOGGER.error("Unable to start application", e);
        }
    }

}
