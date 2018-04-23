package hr.prism.board.api;

import hr.prism.board.mapper.UniversityMapper;
import hr.prism.board.representation.UniversityRepresentation;
import hr.prism.board.service.UniversityService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.List;

import static java.util.stream.Collectors.toList;

@RestController
public class UniversityApi {

    private final UniversityService universityService;

    private final UniversityMapper universityMapper;

    @Inject
    public UniversityApi(UniversityService universityService, UniversityMapper universityMapper) {
        this.universityService = universityService;
        this.universityMapper = universityMapper;
    }

    @RequestMapping(value = "/api/universities", method = RequestMethod.GET)
    public List<UniversityRepresentation> lookupUniversities(@RequestParam String query) {
        return universityService.findUniversities(query).stream().map(universityMapper::apply).collect(toList());
    }

}
