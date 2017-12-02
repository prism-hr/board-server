package hr.prism.board.configuration;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;

import java.util.Properties;
import java.util.TimeZone;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseConfiguration.class);

    @Value("${database.host}")
    private String databaseHost;

    @Value("${database.schema}")
    private String databaseSchema;

    @Value("${clean.db.on.startup}")
    private boolean cleanDbOnStartup;

    @Value("${database.migration.on}")
    private boolean databaseMigrationOn;

    @Bean
    @Primary
    public DataSource dataSource() {
        LOGGER.info("Creating datasource using: " + databaseHost);

        HikariConfig hikariConfig = new HikariConfig();
        String timezone = TimeZone.getDefault().getID();
        hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
        hikariConfig.setJdbcUrl("jdbc:mysql://" + databaseHost + "/" + databaseSchema +
            "?useUnicode=yes&characterEncoding=UTF-8&connectionCollation=utf8_general_ci" +
            "&useLegacyDatetimeCode=false&serverTimezone=" + timezone + "&useSSL=false");
        hikariConfig.setUsername("prism");
        hikariConfig.setPassword("pgadmissions");

        hikariConfig.setPoolName("database-connection-pool");
        hikariConfig.setMaxLifetime(600000);
        hikariConfig.setMaximumPoolSize(20);
        hikariConfig.setConnectionTimeout(12000);
        hikariConfig.setAutoCommit(false);
        hikariConfig.setLeakDetectionThreshold(360000);
        return new HikariDataSource(hikariConfig);
    }

    @Bean
    public Flyway flyway(DataSource dataSource) {
        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);

        if (databaseMigrationOn) {
            flyway.setLocations("classpath:database/core", "classpath:database/migration");
        } else {
            flyway.setLocations("classpath:database.core");
        }

        if (cleanDbOnStartup) {
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

}
