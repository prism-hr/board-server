package hr.prism.board;

import com.stormpath.spring.config.StormpathWebSecurityConfigurer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@SpringBootApplication
public class ApplicationConfiguration extends WebSecurityConfigurerAdapter {

    @Value("${database.schema}")
    private String databaseSchema;

    public static void main(String[] args) {
        SpringApplication.run(ApplicationConfiguration.class);
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
                .driverClassName("com.mysql.jdbc.Driver")
                .url("jdbc:mysql://localhost/" + databaseSchema)
                .username("prism")
                .password("pgadmissions")
                .build();
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

}
