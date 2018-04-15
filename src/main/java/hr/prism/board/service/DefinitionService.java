package hr.prism.board.service;

import com.google.common.collect.ImmutableMap;
import hr.prism.board.exception.BoardException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import java.util.*;
import java.util.stream.Collectors;

import static hr.prism.board.enums.OauthProvider.FACEBOOK;
import static hr.prism.board.enums.OauthProvider.LINKEDIN;
import static hr.prism.board.exception.ExceptionCode.INITIALIZATION_ERROR;
import static java.util.Arrays.stream;
import static org.apache.commons.text.WordUtils.uncapitalize;

@Service
public class DefinitionService {

    private static final String TEST_QUERY = "SELECT 1";

    public static TreeMap<String, Object> DEFINITIONS = new TreeMap<>();

    private final String profile;

    private final String appUrl;

    private final String facebookClientId;

    private final String linkedinClientId;

    private final String cloudinaryFolder;

    private final EntityManager entityManager;

    public DefinitionService(@Value("${profile}") String profile, @Value("${app.url}") String appUrl,
                             @Value("${auth.facebook.clientId}") String facebookClientId,
                             @Value("${auth.linkedin.clientId}") String linkedinClientId,
                             @Value("${cloudinary.folder}") String cloudinaryFolder, EntityManager entityManager) {
        this.profile = profile;
        this.appUrl = appUrl;
        this.facebookClientId = facebookClientId;
        this.linkedinClientId = linkedinClientId;
        this.cloudinaryFolder = cloudinaryFolder;
        this.entityManager = entityManager;
    }

    @PostConstruct
    public void populateDefinitions() {
        try {
            // Check that the database connection is running
            entityManager.createNativeQuery(TEST_QUERY).getResultList();
            getDefinitionClasses().forEach(definitionClass -> {
                List<String> values = stream(definitionClass.getEnumConstants())
                    .map(Enum::name)
                    .collect(Collectors.toList());
                DEFINITIONS.put(getDefinitionKey(definitionClass), values);
            });

            DEFINITIONS.put("profile", profile);
            DEFINITIONS.put("applicationUrl", appUrl);
            DEFINITIONS.put("cloudinaryFolder", cloudinaryFolder);

            List<Map<String, Object>> oauthProviders = new ArrayList<>();
            oauthProviders.add(ImmutableMap.of("id", FACEBOOK, "clientId", facebookClientId));
            oauthProviders.add(ImmutableMap.of("id", LINKEDIN, "clientId", linkedinClientId));
            DEFINITIONS.put("oauthProvider", oauthProviders);
        } catch (Exception e) {
            throw new BoardException(INITIALIZATION_ERROR, "Could not initialize application", e);
        }
    }

    @SuppressWarnings("unchecked")
    private Set<Class<? extends Enum<?>>> getDefinitionClasses() {
        Set<Class<? extends Enum<?>>> definitionClasses = new LinkedHashSet<>();
        try {
            ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
            scanner.addIncludeFilter(new AssignableTypeFilter(Enum.class));

            for (BeanDefinition beanDefinition : scanner.findCandidateComponents("hr.prism.board.enums")) {
                Class<?> clazz = Class.forName(beanDefinition.getBeanClassName());
                if (Enum.class.isAssignableFrom(clazz)) {
                    definitionClasses.add((Class<? extends Enum<?>>) clazz);
                }
            }
        } catch (Exception e) {
            throw new Error(e);
        }

        return definitionClasses;
    }

    private String getDefinitionKey(Class<? extends Enum<?>> definitionClass) {
        return uncapitalize(definitionClass.getSimpleName());
    }

}
