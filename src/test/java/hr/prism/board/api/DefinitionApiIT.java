package hr.prism.board.api;

import hr.prism.board.TestContext;
import hr.prism.board.enums.PostVisibility;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

@TestContext
@RunWith(SpringRunner.class)
public class DefinitionApiIT {

    @Inject
    private DefinitionApi definitionApi;

    @Value("${app.url}")
    private String appUrl;

    @Test
    @SuppressWarnings("unchecked")
    // FIXME: this is not a proper test, assert against the whole response
    public void shouldGetDefinitions() {
        TreeMap<String, Object> definitions = definitionApi.getDefinitions();
        List<String> postVisibility = (List<String>) definitions.get("postVisibility");
        Assert.assertThat(postVisibility, Matchers.containsInAnyOrder(Arrays.stream(PostVisibility.values()).map(PostVisibility::name).toArray(String[]::new)));
        Assert.assertNotNull(appUrl);
        Assert.assertEquals(appUrl, definitions.get("applicationUrl"));
    }

}
