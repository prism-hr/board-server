package hr.prism.board;

import hr.prism.board.configuration.DatabaseConfiguration;
import hr.prism.board.configuration.DbTestConfiguration;
import hr.prism.board.configuration.FreeMarkerConfiguration;
import hr.prism.board.configuration.JacksonConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

@Target(TYPE)
@Retention(RUNTIME)

@Inherited
@ActiveProfiles("test")
@SpringBootTest(
    webEnvironment = NONE,
    classes = {BoardApplication.class, DatabaseConfiguration.class, FreeMarkerConfiguration.class,
        JacksonConfiguration.class, DbTestConfiguration.class})
@TestPropertySource(value = {"classpath:application.properties", "classpath:test.properties"})
public @interface DbTestContext {

}
