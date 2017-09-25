package hr.prism.board.service;

import hr.prism.board.domain.University;
import hr.prism.board.enums.State;
import hr.prism.board.repository.UniversityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

@Service
@Transactional
public class UniversityService {

    public static final String UCL = "ucl";

    public static final String UNIVERSITY_COLLEGE_LONDON = "University College London";

    @Inject
    private UniversityRepository universityRepository;

    @Inject
    private ResourceService resourceService;

    public University getUniversity(Long id) {
        return (University) resourceService.findOne(id);
    }

    public University getOrCreateUniversity(String name, String handle) {
        University university = universityRepository.findByNameOrHandle(name, handle);
        if (university == null) {
            university = new University();
            university.setName(name);
            university.setHandle(handle);
            university = universityRepository.save(university);

            resourceService.updateState(university, State.ACCEPTED);
            resourceService.createResourceRelation(university, university);
            resourceService.setIndexDataAndQuarter(university);
            return university;
        }

        return university;
    }

}
