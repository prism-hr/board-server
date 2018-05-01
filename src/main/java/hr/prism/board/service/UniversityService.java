package hr.prism.board.service;

import hr.prism.board.dao.UniversityDAO;
import hr.prism.board.domain.University;
import hr.prism.board.exception.BoardNotFoundException;
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

    private final UniversityDAO universityDAO;

    private final ResourceService resourceService;

    @Inject
    public UniversityService(UniversityDAO universityDAO, ResourceService resourceService) {
        this.universityDAO = universityDAO;
        this.resourceService = resourceService;
    }

    public University getById(Long id) {
        University university = (University) resourceService.getById(id);
        if (university == null) {
            throw new BoardNotFoundException(MISSING_RESOURCE, UNIVERSITY, id);
        }

        return university;
    }

    public List<ResourceSearch> findUniversities(String searchTerm) {
        return universityDAO.findUniversities(searchTerm);
    }

}
