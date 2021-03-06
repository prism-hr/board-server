package hr.prism.board.mapper;
import hr.prism.board.domain.University;
import hr.prism.board.representation.UniversityRepresentation;
import hr.prism.board.value.ResourceSearch;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.function.Function;


@Component
public class UniversityMapper implements Function<University, UniversityRepresentation> {

    private final ResourceMapper resourceMapper;

    private final DocumentMapper documentMapper;

    @Inject
    public UniversityMapper(ResourceMapper resourceMapper, DocumentMapper documentMapper) {
        this.resourceMapper = resourceMapper;
        this.documentMapper = documentMapper;
    }

    @Override
    public UniversityRepresentation apply(University university) {
        if (university == null) {
            return null;
        }

        return resourceMapper.applySmall(university, UniversityRepresentation.class)
            .setHomepage(university.getHomepage())
            .setDocumentLogo(documentMapper.apply(university.getDocumentLogo()))
            .setHandle(university.getHandle());
    }

    public UniversityRepresentation apply(ResourceSearch university) {
        if (university == null) {
            return null;
        }

        return new UniversityRepresentation()
            .setId(university.getId())
            .setName(university.getName())
            .setDocumentLogo(documentMapper.apply(university));
    }

}
