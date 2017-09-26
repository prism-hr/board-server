package hr.prism.board.mapper;

import hr.prism.board.domain.University;
import hr.prism.board.representation.UniversityRepresentation;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class UniversityMapper implements Function<University, UniversityRepresentation> {

    @Override
    public UniversityRepresentation apply(University university) {
        if (university == null) {
            return null;
        }

        return new UniversityRepresentation()
            .setId(university.getId())
            .setName(university.getName())
            .setHandle(university.getHandle());
    }

}
