package hr.prism.board.api;

import hr.prism.board.service.DefinitionService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.Map;

@RestController
public class DefinitionApi {
    
    @Inject
    private DefinitionService definitionService;
    
    @RequestMapping(value = "/api/definitions", method = RequestMethod.GET)
    public Map<String, Object> getDefinitions() {
        return definitionService.getDefinitions();
    }
    
}
