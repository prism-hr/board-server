package hr.prism.board;

import com.stormpath.sdk.servlet.mvc.WebHandler;
import com.stormpath.spring.config.StormpathWebSecurityConfigurer;
import hr.prism.board.repository.MyRepositoryImpl;
import hr.prism.board.service.UserService;
import org.flywaydb.core.Flyway;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@SpringBootApplication
@EnableJpaRepositories(repositoryBaseClass = MyRepositoryImpl.class)
public class ApplicationConfiguration extends WebSecurityConfigurerAdapter {

    @Inject
    private Environment environment;

    @Inject
    private UserService userService;

    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(ApplicationConfiguration.class);
        springApplication.run(args);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.apply(StormpathWebSecurityConfigurer.stormpath())
                .and().authorizeRequests()
                .antMatchers("/**").permitAll()
                .antMatchers("/api/**").fullyAuthenticated();
    }

    @Bean
    public DataSource dataSource() {
        return DataSourceBuilder.create()
                .driverClassName("com.mysql.cj.jdbc.Driver")
                .url("jdbc:mysql://" + environment.getProperty("database.host") + "/" + environment.getProperty("database.schema") +
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

    @Bean
    public Jackson2ObjectMapperBuilder objectMapperBuilder() {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        builder.indentOutput(true);
        return builder;
    }
}
