package hr.prism.board.service;

import hr.prism.board.domain.University;
import hr.prism.board.enums.State;
import hr.prism.board.repository.UniversityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;

@Service
@Transactional
@SuppressWarnings("SpringAutowiredFieldsWarningInspection")
public class UniversityService {

    @Inject
    private UniversityRepository universityRepository;

    @Inject
    private ResourceService resourceService;

    public University getUniversity(Long id) {
        return (University) resourceService.findOne(id);
    }

    @SuppressWarnings("SameParameterValue")
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

    List<University> findAll() {
        return universityRepository.findAll();
    }

}
