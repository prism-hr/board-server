package hr.prism.board;

import hr.prism.board.configuration.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@Target(TYPE)
@Retention(RUNTIME)

@Inherited
@AutoConfigureMockMvc
@ActiveProfiles("test")
@SpringBootTest(
    webEnvironment = RANDOM_PORT,
    classes = {BoardApplication.class, DatabaseConfiguration.class, FreeMarkerConfiguration.class,
        JacksonConfiguration.class, SecurityConfiguration.class, WebMvcConfiguration.class, TestConfiguration.class})
@TestPropertySource(value = {"classpath:application.properties", "classpath:test.properties"})
public @interface ApiTestContext {

}
