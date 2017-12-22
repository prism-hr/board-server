package hr.prism.board.mapper;

import hr.prism.board.domain.University;
import hr.prism.board.representation.UniversityRepresentation;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.function.Function;

@Service
@SuppressWarnings("SpringAutowiredFieldsWarningInspection")
public class UniversityMapper implements Function<University, UniversityRepresentation> {

    @Inject
    private DocumentMapper documentMapper;

    @Override
    public UniversityRepresentation apply(University university) {
        if (university == null) {
            return null;
        }

        return new UniversityRepresentation()
            .setId(university.getId())
            .setName(university.getName())
            .setDocumentLogo(documentMapper.apply(university.getDocumentLogo()))
            .setHandle(university.getHandle());
    }

}
