package hr.prism.board.api;

import hr.prism.board.ApplicationConfiguration;
import hr.prism.board.enums.PostVisibility;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

@AutoConfigureMockMvc
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {ApplicationConfiguration.class})
@TestPropertySource(value = {"classpath:application.properties", "classpath:test.properties"})
public class SystemApiIT {

    @Inject
    private Environment environment;

    @Inject
    private SystemApi systemApi;

    @Test
    @SuppressWarnings("unchecked")
    public void shouldGetDefinitions() {
        TreeMap<String, Object> definitions = systemApi.getDefinitions();
        List<String> postVisibility = (List<String>) definitions.get("postVisibility");
        Assert.assertThat(postVisibility, Matchers.containsInAnyOrder(Arrays.stream(PostVisibility.values()).map(PostVisibility::name).toArray(String[]::new)));

        String applicationUrl = environment.getProperty("app.url");
        Assert.assertNotNull(applicationUrl);
        Assert.assertEquals(applicationUrl, definitions.get("applicationUrl"));
    }

}
