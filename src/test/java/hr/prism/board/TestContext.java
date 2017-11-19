package hr.prism.board;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import hr.prism.board.configuration.CacheConfiguration;
import hr.prism.board.configuration.DatabaseConfiguration;
import hr.prism.board.configuration.SecurityConfiguration;
import hr.prism.board.configuration.TestConfiguration;
import hr.prism.board.configuration.WebMvcConfiguration;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)

@Inherited
@AutoConfigureMockMvc
@ActiveProfiles("test")
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = {BoardApplication.class, CacheConfiguration.class, DatabaseConfiguration.class,
        SecurityConfiguration.class, WebMvcConfiguration.class, TestConfiguration.class})
@TestPropertySource(value = {"classpath:application.properties", "classpath:test.properties"})
public @interface TestContext {

}
