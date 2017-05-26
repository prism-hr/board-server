package hr.prism.board;

import com.google.common.collect.ImmutableMap;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sendgrid.SendGrid;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;

import javax.inject.Inject;
import javax.sql.DataSource;

import hr.prism.board.repository.MyRepositoryImpl;

@EnableAsync
@EnableWebMvc
@EnableCaching
@Configuration
@EnableScheduling
@SpringBootApplication
@EnableJpaRepositories(repositoryBaseClass = MyRepositoryImpl.class)
@Import(SecurityConfiguration.class)
public class BoardApplication extends WebMvcConfigurerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(BoardApplication.class);

    @Inject
    private Environment environment;

    public static void main(String[] args) {
        BufferedWriter writer = null;
        InputStream propertiesStream = null;

        try {
            ClassLoader classLoader = BoardApplication.class.getClassLoader();
            Properties properties = new Properties();
            propertiesStream = classLoader.getResourceAsStream("application.properties");
            properties.load(propertiesStream);

            String jwsSecret;
            String userHome = System.getProperty("user.home");
            File secretFile = new File(userHome + "/jws.secret");
            if (secretFile.exists()) {
                jwsSecret = IOUtils.toString(secretFile.toURI(), StandardCharsets.UTF_8);
            } else {
                writer = new BufferedWriter(new FileWriter(userHome + "/jws.secret"));
                jwsSecret = RandomStringUtils.randomAlphanumeric(256);
                writer.write(jwsSecret);
            }

            SpringApplication springApplication = new SpringApplication(BoardApplication.class);
            springApplication.setAdditionalProfiles(properties.get("profile").toString());
            springApplication.setDefaultProperties(ImmutableMap.of("jws.secret", jwsSecret));
            springApplication.run(args);
        } catch (Exception e) {
            LOGGER.error("Unable to start application", e);
        } finally {
            IOUtils.closeQuietly(propertiesStream);
            IOUtils.closeQuietly(writer);
        }
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
        flyway.setLocations("classpath:database");

        String[] activeProfiles = environment.getActiveProfiles();
        if (activeProfiles.length > 0 && activeProfiles[0].equals("test")) {
            flyway.clean();
        }

        flyway.migrate();
        return flyway;
    }

    @Bean
    public SendGrid sendGrid() {
        return new SendGrid(environment.getProperty("sendgrid.key"));
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
    public CacheManager cacheManager() {
        return new EhCacheCacheManager(ehCacheCacheManager().getObject());
    }

    @Bean
    public EhCacheManagerFactoryBean ehCacheCacheManager() {
        EhCacheManagerFactoryBean ehCacheManager = new EhCacheManagerFactoryBean();
        ehCacheManager.setConfigLocation(new ClassPathResource("ehcache.xml"));
        ehCacheManager.setShared(true);
        return ehCacheManager;
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new Jdk8Module());
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.enable(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS);
        objectMapper.enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(new MappingJackson2HttpMessageConverter(objectMapper()));
    }

}
