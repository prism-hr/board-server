package hr.prism.board.mapper;

import hr.prism.board.domain.User;
import hr.prism.board.representation.UserRepresentation;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class UserMapper implements Function<User, UserRepresentation> {
    
    @Override
    public UserRepresentation apply(User user) {
        if (user == null) {
            return null;
        }
        
        return new UserRepresentation()
            .setId(user.getId())
            .setGivenName(user.getGivenName())
            .setSurname(user.getSurname())
            .setEmail(user.getEmail());
    }
    
}
