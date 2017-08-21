package hr.prism.board.service;

import com.google.common.collect.ImmutableMap;
import hr.prism.board.enums.OauthProvider;
import org.apache.commons.text.WordUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DefinitionService {

    @SuppressWarnings("unchecked")
    private TreeMap<String, Object> definitions;

    @Value("${app.url}")
    private String appUrl;

    @Value("${auth.facebook.clientId}")
    private String facebookClientId;

    @Value("${auth.linkedin.clientId}")
    private String linkedinClientId;

    @PostConstruct
    public TreeMap<String, Object> getDefinitions() {
        if (this.definitions == null) {
            this.definitions = new TreeMap<>();
            getDefinitionClasses().forEach(definitionClass -> {
                List<String> values = Arrays.stream(definitionClass.getEnumConstants()).map(Enum::name).collect(Collectors.toList());
                this.definitions.put(getDefinitionKey(definitionClass), values);
            });

            this.definitions.put("applicationUrl", appUrl);
            List<Map<String, Object>> oauthProviders = new ArrayList<>();
            oauthProviders.add(ImmutableMap.of("id", OauthProvider.FACEBOOK, "clientId", facebookClientId));
            oauthProviders.add(ImmutableMap.of("id", OauthProvider.LINKEDIN, "clientId", linkedinClientId));
            this.definitions.put("oauthProvider", oauthProviders);
        }

        return this.definitions;
    }

    @SuppressWarnings("unchecked")
    private Set<Class<? extends Enum<?>>> getDefinitionClasses() {
        Set<Class<? extends Enum<?>>> definitionClasses = new LinkedHashSet<>();
        try {
            ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
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
        return WordUtils.uncapitalize(definitionClass.getSimpleName());
    }

}
