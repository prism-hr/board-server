package hr.prism.board.api;

import hr.prism.board.TestContext;
import hr.prism.board.enums.*;
import hr.prism.board.enums.Labels.Label;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@TestContext
@RunWith(SpringRunner.class)
@SuppressWarnings("unchecked")
public class DefinitionApiIT {

    @Inject
    private DefinitionApi definitionApi;

    @Value("${profile}")
    private String profile;

    @Value("${app.url}")
    private String appUrl;

    @Value("${cloudinary.folder}")
    private String cloudinaryFolder;

    @Value("${auth.facebook.clientId}")
    private String facebookClientId;

    @Value("${auth.linkedin.clientId}")
    private String linkedinClientId;

    @Test
    public void shouldGetDefinitions() throws NoSuchFieldException {
        TreeMap<String, Object> definitions = definitionApi.getDefinitions();
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

        verifyProperty(definitions, "profile", profile);
        verifyProperty(definitions, "applicationUrl", appUrl);
        verifyProperty(definitions, "cloudinaryFolder", cloudinaryFolder);

        List<Map<String, Object>> oauthProviders = (List<Map<String, Object>>) definitions.get("oauthProvider");
        Map<String, Object> facebook = oauthProviders.get(0);
        verifyProperty(facebook, "id", OauthProvider.FACEBOOK);
        verifyProperty(facebook, "clientId", facebookClientId);
        Map<String, Object> linkedin = oauthProviders.get(1);
        verifyProperty(linkedin, "id", OauthProvider.LINKEDIN);
        verifyProperty(linkedin, "clientId", linkedinClientId);
    }

    private <T extends Enum<T>> void verifyDefinition(TreeMap<String, Object> definitions, String key, Class<T> definitionClass) throws NoSuchFieldException {
        List<Object> values = (List<Object>) definitions.get(key);
        List<String> constants = Stream.of(definitionClass.getEnumConstants()).map(Enum::name).collect(Collectors.toList());
        for (Object value : values) {
            if (value instanceof String) {
                Assert.assertTrue(constants.contains(value));
            } else {
                Map<String, Map<String, String>> valueMap = (Map<String, Map<String, String>>) value;
                String valueString = valueMap.keySet().iterator().next();
                Assert.assertTrue(constants.contains(valueString));

                Labels labels = definitionClass.getField(valueString).getAnnotation(Labels.class);
                Map<String, String> labelMap = new TreeMap<>();
                Stream.of(labels.value()).forEach(label -> labelMap.put(label.scope().name(), label.value()));
                Assert.assertEquals(labelMap, valueMap.values().iterator().next());
            }
        }
    }

    private void verifyProperty(Map<String, Object> definitions, String key, Object value) {
        Assert.assertNotNull(value);
        Assert.assertEquals(value, definitions.get(key));
    }

}
