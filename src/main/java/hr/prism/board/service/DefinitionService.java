package hr.prism.board.service;

import com.google.common.collect.Sets;
import com.stormpath.sdk.lang.Strings;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.env.Environment;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class DefinitionService {
    
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
        }
        return definitions;
    }
    
    @SuppressWarnings("unchecked")
    private Set<Class<? extends Enum<?>>> getDefinitionClasses() {
        Set<Class<? extends Enum<?>>> definitionClasses = Sets.newLinkedHashSet();
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
        return Strings.uncapitalize(definitionClass.getSimpleName());
    }
    
}
