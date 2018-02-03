package hr.prism.board.service;

import hr.prism.board.domain.Document;
import hr.prism.board.domain.University;
import hr.prism.board.dto.DocumentDTO;
import hr.prism.board.enums.Scope;
import hr.prism.board.enums.State;
import hr.prism.board.repository.UniversityRepository;
import hr.prism.board.representation.DocumentRepresentation;
import hr.prism.board.representation.UniversityRepresentation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@SuppressWarnings("SpringAutowiredFieldsWarningInspection")
public class UniversityService {

    @SuppressWarnings("SqlResolve")
    private static final String SIMILAR_UNIVERSITY =
        "SELECT resource.id, resource.name, document_logo.cloudinary_id, document_logo.cloudinary_url, document_logo.file_name, " +
            "IF(resource.name LIKE :searchTermHard, 1, 0) AS similarityHard, " +
            "MATCH (resource.name) AGAINST(:searchTermSoft IN BOOLEAN MODE) AS similaritySoft " +
            "FROM resource " +
            "LEFT JOIN document AS document_logo " +
            "ON resource.document_logo_id = document_logo.id " +
            "WHERE resource.scope = :scope AND resource.state = :state " +
            "HAVING similarityHard = 1 OR similaritySoft > 0 " +
            "ORDER BY similarityHard DESC, similaritySoft DESC, resource.name " +
            "LIMIT 10";

    @Inject
    private UniversityRepository universityRepository;

    @Inject
    private ResourceService resourceService;

    @Inject
    private DocumentService documentService;

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    @SuppressWarnings("SpringJavaAutowiringInspection")
    private PlatformTransactionManager platformTransactionManager;

    public University getUniversity(Long id) {
        return (University) resourceService.findOne(id);
    }

    public University getOrCreateUniversity(String name, String handle) {
        return getOrCreateUniversity(name, handle, null);
    }

    @SuppressWarnings("SameParameterValue")
    public University getOrCreateUniversity(String name, String handle, DocumentDTO documentLogoDTO) {
        University university = universityRepository.findByNameOrHandle(name, handle);
        if (university == null) {
            university = new University();
            university.setName(name);
            university.setHandle(handle);
            university = universityRepository.save(university);

            resourceService.updateState(university, State.ACCEPTED);
            resourceService.createResourceRelation(university, university);
            resourceService.setIndexDataAndQuarter(university);

            if (documentLogoDTO != null) {
                Document documentLogo = documentService.getOrCreateDocument(documentLogoDTO);
                university.setDocumentLogo(documentLogo);
            }

            return university;
        }

        return university;
    }

    @SuppressWarnings("unchecked")
    public List<UniversityRepresentation> findBySimilarName(String searchTerm) {
        List<Object[]> rows = new TransactionTemplate(platformTransactionManager).execute(status ->
            entityManager.createNativeQuery(SIMILAR_UNIVERSITY)
                .setParameter("searchTermHard", searchTerm + "%")
                .setParameter("searchTermSoft", searchTerm)
                .setParameter("scope", Scope.UNIVERSITY.name())
                .setParameter("state", State.ACCEPTED.name())
                .getResultList());

        List<UniversityRepresentation> universityRepresentations = new ArrayList<>();
        for (Object[] row : rows) {
            UniversityRepresentation universityRepresentation =
                new UniversityRepresentation().setId(Long.parseLong(row[0].toString())).setName(row[1].toString());
            Object cloudinaryId = row[2];
            if (cloudinaryId != null) {
                DocumentRepresentation documentLogoRepresentation =
                    new DocumentRepresentation().setCloudinaryId(cloudinaryId.toString()).setCloudinaryUrl(row[3].toString()).setFileName(row[4].toString());
                universityRepresentation.setDocumentLogo(documentLogoRepresentation);
            }

            universityRepresentations.add(universityRepresentation);
        }

        return universityRepresentations;
    }

}
