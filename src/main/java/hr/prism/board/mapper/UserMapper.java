package hr.prism.board.mapper;

import hr.prism.board.domain.User;
import hr.prism.board.representation.UserRepresentation;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.function.Function;

@Component
public class UserMapper implements Function<User, UserRepresentation> {

    private final DocumentMapper documentMapper;

    private final LocationMapper locationMapper;

    @Inject
    public UserMapper(DocumentMapper documentMapper, LocationMapper locationMapper) {
        this.documentMapper = documentMapper;
        this.locationMapper = locationMapper;
    }

    @Override
    public UserRepresentation apply(User user) {
        if (user == null) {
            return null;
        }

        return new UserRepresentation()
            .setId(user.getId())
            .setGivenName(user.getGivenName())
            .setSurname(user.getSurname())
            .setEmail(user.isRevealEmail() ? user.getEmail() : user.getEmailDisplay())
            .setDocumentImage(documentMapper.apply(user.getDocumentImage()))
            .setDocumentImageRequestState(user.getDocumentImageRequestState())
            .setSeenWalkThrough(user.getSeenWalkThrough())
            .setGender(user.getGender())
            .setAgeRange(user.getAgeRange())
            .setLocationNationality(locationMapper.apply(user.getLocationNationality()))
            .setDocumentResume(documentMapper.apply(user.getDocumentResume()))
            .setWebsiteResume(user.getWebsiteResume())
            .setScopes(user.getScopes())
            .setDefaultOrganizationName(user.getDefaultOrganizationName())
            .setDefaultLocation(locationMapper.apply(user.getDefaultLocation()))
            .setRegistered(user.isRegistered());
    }

}
