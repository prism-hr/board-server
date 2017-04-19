package hr.prism.board.api;

import hr.prism.board.service.DefinitionService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.TreeMap;

@RestController
public class SystemApi {
    
    @Inject
    private DefinitionService definitionService;
    
    @RequestMapping(value = "/definitions", method = RequestMethod.GET)
    public TreeMap<String, Object> getDefinitions() {
        return definitionService.getDefinitions();
    }
    
}
