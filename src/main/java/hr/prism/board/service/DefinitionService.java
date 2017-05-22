package hr.prism.board.service;

import com.google.common.collect.Sets;
import hr.prism.board.enums.OauthProvider;
import org.apache.commons.lang3.text.WordUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.env.Environment;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class DefinitionService {

    @SuppressWarnings("unchecked")
    Set<Class<? extends Enum<?>>> definitionClasses = Sets.newLinkedHashSet();
    private TreeMap<String, Object> definitions;
    @Inject
    private Environment environment;

    @PostConstruct
    public TreeMap<String, Object> getDefinitions() {
        if (definitions == null) {
            definitions = new TreeMap<>();
            getDefinitionClasses().forEach(definitionClass -> {
                List<String> values = Arrays.stream(definitionClass.getEnumConstants()).map(Enum::name).collect(Collectors.toList());
                definitions.put(getDefinitionKey(definitionClass), values);
            });
            definitions.put("applicationUrl", environment.getProperty("app.url"));
            List<Map<String, Object>> clientIdMap = Stream.of(OauthProvider.values())
                .map(provider -> {
                    Map<String, Object> providerMap = new HashMap<>();
                    providerMap.put("id", provider);
                    String clientId = environment.getProperty("auth." + provider.name().toLowerCase() + ".clientId");
                    if (clientId != null) {
                        providerMap.put("clientId", clientId);
                    }
                    return providerMap;
                })
                .collect(Collectors.toList());
            definitions.put("oauthProvider", clientIdMap);
        }
        return definitions;
    }

    private Set<Class<? extends Enum<?>>> getDefinitionClasses() {
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
