package hr.prism.board.api;

import hr.prism.board.representation.UniversityRepresentation;
import hr.prism.board.service.UniversityService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.List;

@RestController
@SuppressWarnings("SpringAutowiredFieldsWarningInspection")
public class UniversityApi {

    @Inject
    private UniversityService universityService;

    @RequestMapping(value = "/api/universities", method = RequestMethod.GET, params = "query")
    public List<UniversityRepresentation> lookupUniversities(@RequestParam String query) {
        return universityService.findBySimilarName(query);
    }

}
