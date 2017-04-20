package hr.prism.board;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.stormpath.sdk.servlet.mvc.WebHandler;
import hr.prism.board.repository.MyRepositoryImpl;
import hr.prism.board.service.UserService;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.util.List;
import java.util.Properties;

@EnableWebMvc
@Configuration
@EnableScheduling
@SpringBootApplication
@EnableJpaRepositories(repositoryBaseClass = MyRepositoryImpl.class)
@Import(SecurityConfiguration.class)
public class ApplicationConfiguration extends WebMvcConfigurerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationConfiguration.class);

    @Inject
    private Environment environment;

    @Inject
    private UserService userService;

    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(ApplicationConfiguration.class);
        springApplication.run(args);
    }

    @Bean
    public DataSource dataSource() {
        String host = environment.getProperty("database.host");
        LOGGER.info("Creating datasource using: " + host);

        return DataSourceBuilder.create()
            .driverClassName("com.mysql.cj.jdbc.Driver")
            .url("jdbc:mysql://" + host + "/" + environment.getProperty("database.schema") +
                "?useUnicode=yes&characterEncoding=UTF-8&connectionCollation=utf8_general_ci&useLegacyDatetimeCode=false&serverTimezone=UTC&useSSL=false")
            .username("prism")
            .password("pgadmissions")
            .build();
    }

    @Bean
    public Flyway flyway(DataSource dataSource) {
        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.setLocations("classpath:db/migration");

        String[] activeProfiles = environment.getActiveProfiles();
        if (activeProfiles.length > 0 && activeProfiles[0].equals("test")) {
            flyway.clean();
        }

        flyway.migrate();
        return flyway;
    }

    @Bean
    public LocalSessionFactoryBean sessionFactory() {
        LocalSessionFactoryBean sessionFactoryBean = new LocalSessionFactoryBean();
        sessionFactoryBean.setDataSource(dataSource());
        sessionFactoryBean.setPackagesToScan("hr.prism.board.domain");
        Properties hibernateProperties = new Properties();
        hibernateProperties.put("hibernate.dialect", "org.hibernate.dialect.MySQL5Dialect");
        hibernateProperties.put("hibernate.show_sql", true);
        sessionFactoryBean.setHibernateProperties(hibernateProperties);
        return sessionFactoryBean;
    }

    @Bean
    public WebHandler registerPostHandler() {
        return (request, response, account) -> {
            userService.createUser(account);
            return true;
        };
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        builder.modules(new Jdk8Module());
        builder.modules(new JavaTimeModule());
        builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        builder.indentOutput(true);
        converters.add(new MappingJackson2HttpMessageConverter(builder.build()));
    }

}
