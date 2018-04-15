package hr.prism.board.api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.TreeMap;

import static hr.prism.board.service.DefinitionService.DEFINITIONS;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
public class DefinitionApi {

    @RequestMapping(value = "/api/definitions", method = GET)
    public TreeMap<String, Object> getDefinitions() {
        return DEFINITIONS;
    }

}
