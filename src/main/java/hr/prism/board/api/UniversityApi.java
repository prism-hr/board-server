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
public class UniversityApi {

    private final UniversityService universityService;

    @Inject
    public UniversityApi(UniversityService universityService) {
        this.universityService = universityService;
    }

    @RequestMapping(value = "/api/universities", method = RequestMethod.GET)
    public List<UniversityRepresentation> lookupUniversities(@RequestParam String query) {
        return universityService.findBySimilarName(query);
    }

}
