package hr.prism.board.api;

import hr.prism.board.service.DefinitionService;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.List;
import java.util.TreeMap;

@RestController
public class SystemApi {
    
    @Inject
    private Environment environment;
    
    @Inject
    private DefinitionService definitionService;
    
    @RequestMapping(value = "/profile", method = RequestMethod.GET)
    public String getApplicationProfile() {
        return environment.getProperty("id");
    }
    
    @RequestMapping(value = "/definitions", method = RequestMethod.GET)
    public TreeMap<String, List<String>> getDefinitions() {
        return definitionService.getDefinitions();
    }
    
}
