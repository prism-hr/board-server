package hr.prism.board.mapper;

import hr.prism.board.domain.User;
import hr.prism.board.representation.UserRepresentation;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.function.Function;

@Service
public class UserMapper implements Function<User, UserRepresentation> {

    @Inject
    private DocumentMapper documentMapper;

    @Override
    public UserRepresentation apply(User user) {
        if (user == null) {
            return null;
        }

        return new UserRepresentation()
            .setId(user.getId())
            .setGivenName(user.getGivenName())
            .setSurname(user.getSurname())
            .setEmail(user.getEmail())
            .setDocumentImage(documentMapper.apply(user.getDocumentImage()));
    }

}
