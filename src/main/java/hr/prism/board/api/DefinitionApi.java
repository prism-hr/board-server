package hr.prism.board.api;

import hr.prism.board.service.DefinitionService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.TreeMap;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
public class DefinitionApi {

    private final DefinitionService definitionService;

    @Inject
    public DefinitionApi(DefinitionService definitionService) {
        this.definitionService = definitionService;
    }

    @RequestMapping(value = "/api/definitions", method = GET)
    public TreeMap<String, Object> getDefinitions() {
        return definitionService.getDefinitions();
    }

}
