package hr.prism.board.api;

import hr.prism.board.TestContext;
import hr.prism.board.enums.*;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@TestContext
@RunWith(SpringRunner.class)
@SuppressWarnings("unchecked")
public class DefinitionApiIT {

    @Inject
    private DefinitionApi definitionApi;

    @Value("${app.url}")
    private String appUrl;

    @Value("${auth.facebook.clientId}")
    private String facebookClientId;

    @Value("${auth.linkedin.clientId}")
    private String linkedinClientId;

    @Value("${cloudinary.folder}")
    private String cloudinaryFolder;

    @Test
    public void shouldGetDefinitions() {
        TreeMap<String, Object> definitions = definitionApi.getDefinitions();
        verifyProperty(definitions, "applicationUrl", appUrl);
        verifyProperty(definitions, "cloudinaryFolder", cloudinaryFolder);

        verifyDefinition(definitions, "action", Action.class);
        verifyDefinition(definitions, "activity", Activity.class);
        verifyDefinition(definitions, "activityEvent", ActivityEvent.class);
        verifyDefinition(definitions, "badgeListType", BadgeListType.class);
        verifyDefinition(definitions, "badgeType", BadgeType.class);
        verifyDefinition(definitions, "categoryType", CategoryType.class);
        verifyDefinition(definitions, "documentRequestState", DocumentRequestState.class);
        verifyDefinition(definitions, "existingRelation", ExistingRelation.class);
        verifyDefinition(definitions, "memberCategory", MemberCategory.class);
        verifyDefinition(definitions, "notification", Notification.class);
        verifyDefinition(definitions, "resourceEvent", ResourceEvent.class);
        verifyDefinition(definitions, "role", Role.class);
        verifyDefinition(definitions, "scope", Scope.class);
        verifyDefinition(definitions, "state", State.class);

        List<Map<String, Object>> oauthProviders = (List<Map<String, Object>>) definitions.get("oauthProvider");
        Map<String, Object> facebook = oauthProviders.get(0);
        verifyProperty(facebook, "id", OauthProvider.FACEBOOK);
        verifyProperty(facebook, "clientId", facebookClientId);
        Map<String, Object> linkedin = oauthProviders.get(1);
        verifyProperty(linkedin, "id", OauthProvider.LINKEDIN);
        verifyProperty(linkedin, "clientId", linkedinClientId);
    }

    private void verifyProperty(Map<String, Object> definitions, String key, Object value) {
        Assert.assertNotNull(value);
        Assert.assertEquals(value, definitions.get(key));
    }

    private <T extends Enum<T>> void verifyDefinition(TreeMap<String, Object> definitions, String key, Class<T> clazz) {
        Assert.assertThat((List<String>) definitions.get(key), Matchers.containsInAnyOrder(Arrays.stream(clazz.getEnumConstants()).map(Enum::name).toArray(String[]::new)));
    }

}
