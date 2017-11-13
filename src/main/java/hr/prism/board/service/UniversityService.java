package hr.prism.board.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.google.common.collect.ImmutableMap;
import hr.prism.board.domain.Document;
import hr.prism.board.domain.University;
import hr.prism.board.dto.DocumentDTO;
import hr.prism.board.enums.State;
import hr.prism.board.repository.UniversityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@SuppressWarnings("SpringAutowiredFieldsWarningInspection")
public class UniversityService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UniversityService.class);

    @Inject
    private UniversityRepository universityRepository;

    @Inject
    private ResourceService resourceService;

    @Inject
    private DocumentService documentService;

    public void migrate(Long universityId) throws IOException {
        Cloudinary cloudinary = new Cloudinary(
            ObjectUtils.asMap(
                "cloud_name", "board-prism-hr",
                "api_key", "218993381117874",
                "api_secret", "kc3ZMH_p2UNW2epg4XaphEFCAks"));

        University university = (University) resourceService.findOne(universityId);
        String universityString = university.toString();
        LOGGER.info("Started migrating: " + universityString);

        try {
            if (university.getHandle() == null) {
                university.setHandle(resourceService.createHandle(null, university.getName(), universityRepository::findHandleByLikeSuggestedHandle));
            }

            Document documentLogo = university.getDocumentLogo();
            if (documentLogo == null) {
                String domain = university.getHomepage().replace("https://www.", "");
                Map upload = cloudinary.uploader().upload("https://logo.clearbit.com/" + domain + "?size=200", ImmutableMap.of("folder", "university"));
                documentLogo = documentService.getOrCreateDocument(
                    new DocumentDTO().setCloudinaryId(upload.get("public_id").toString()).setCloudinaryUrl(upload.get("secure_url").toString()).setFileName("logo.png"));
                university.setDocumentLogo(documentLogo);
            }

            resourceService.createResourceRelation(university, university);
            resourceService.setIndexDataAndQuarter(university);
            LOGGER.info("Finished migrating: " + universityString);
        } catch (Throwable t) {
            LOGGER.error("Error migrating " + universityString, t);
        }
    }

    List<Long> findAllIds() {
        return universityRepository.findAllIds();
    }

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
