package hr.prism.board.configuration;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.util.TimeZone;

import static org.slf4j.LoggerFactory.getLogger;

@EnableJpaRepositories(basePackages = "hr.prism.board.repository")
public class DatabaseConfiguration {

    private static final Logger LOGGER = getLogger(DatabaseConfiguration.class);

    private final String databaseHost;

    private final String databaseSchema;

    private final Integer databaseCpMaxLifetime;

    private final Integer databaseCpMaximumPoolSize;

    private final Integer databaseCpConnectionTimeout;

    private final Integer databaseCpLeakDetectionThreshold;

    private final String databasePassword;

    private final boolean cleanDbOnStartup;

    private final boolean databaseMigrationOn;

    @Inject
    public DatabaseConfiguration(
        @Value("${database.host}") String databaseHost, @Value("${database.schema}") String databaseSchema,
        @Value("${database.connection.pool.maxLifetime}") Integer databaseCpMaxLifetime,
        @Value("${database.connection.pool.maximumPoolSize}") Integer databaseCpMaximumPoolSize,
        @Value("${database.connection.pool.connectionTimeout}") Integer databaseCpConnectionTimeout,
        @Value("${database.connection.pool.leakDetectionThreshold}") Integer databaseCpLeakDetectionThreshold,
        @Value("${database.password}") String databasePassword, @Value("${clean.db.on.startup}") boolean cleanDbOnStartup,
        @Value("${database.migration.on}") boolean databaseMigrationOn) {
        this.databaseHost = databaseHost;
        this.databaseSchema = databaseSchema;
        this.databaseCpMaxLifetime = databaseCpMaxLifetime;
        this.databaseCpMaximumPoolSize = databaseCpMaximumPoolSize;
        this.databaseCpConnectionTimeout = databaseCpConnectionTimeout;
        this.databaseCpLeakDetectionThreshold = databaseCpLeakDetectionThreshold;
        this.databasePassword = databasePassword;
        this.cleanDbOnStartup = cleanDbOnStartup;
        this.databaseMigrationOn = databaseMigrationOn;
    }

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
        hikariConfig.setPassword(databasePassword);

        hikariConfig.setPoolName("database-connection-pool");
        hikariConfig.setMaxLifetime(databaseCpMaxLifetime);
        hikariConfig.setMaximumPoolSize(databaseCpMaximumPoolSize);
        hikariConfig.setConnectionTimeout(databaseCpConnectionTimeout);
        hikariConfig.setAutoCommit(false);
        hikariConfig.setLeakDetectionThreshold(databaseCpLeakDetectionThreshold);
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

        flyway.setOutOfOrder(true);
        if (cleanDbOnStartup) {
            flyway.clean();
        }

        flyway.migrate();
        return flyway;
    }

}
