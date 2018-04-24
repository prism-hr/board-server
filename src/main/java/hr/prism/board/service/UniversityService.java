package hr.prism.board.service;

import hr.prism.board.dao.UniversityDAO;
import hr.prism.board.domain.University;
import hr.prism.board.enums.State;
import hr.prism.board.exception.BoardNotFoundException;
import hr.prism.board.repository.UniversityRepository;
import hr.prism.board.value.ResourceSearch;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;

import static hr.prism.board.enums.Scope.UNIVERSITY;
import static hr.prism.board.exception.ExceptionCode.MISSING_RESOURCE;

@Service
@Transactional
public class UniversityService {

    private final UniversityRepository universityRepository;

    private final UniversityDAO universityDAO;

    private final ResourceService resourceService;

    @Inject
    public UniversityService(UniversityRepository universityRepository, UniversityDAO universityDAO,
                             ResourceService resourceService) {
        this.universityRepository = universityRepository;
        this.universityDAO = universityDAO;
        this.resourceService = resourceService;
    }

    public University getByIdWithExistenceCheck(Long id) {
        University university = (University) resourceService.getById(id);
        if (university == null) {
            throw new BoardNotFoundException(MISSING_RESOURCE, UNIVERSITY, id);
        }

        return university;
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

    public List<ResourceSearch> findUniversities(String searchTerm) {
        return universityDAO.findUniversities(searchTerm);
    }

}
