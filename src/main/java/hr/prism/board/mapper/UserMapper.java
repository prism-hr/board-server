package hr.prism.board.mapper;

import hr.prism.board.domain.User;
import hr.prism.board.enums.Role;
import hr.prism.board.enums.Scope;
import hr.prism.board.representation.UserRepresentation;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;
import java.util.function.Function;

import static hr.prism.board.enums.Role.NON_MEMBER_ROLES;
import static java.util.stream.Collectors.toList;

@Component
public class UserMapper implements Function<User, UserRepresentation> {

    private final DocumentMapper documentMapper;

    private final OrganizationMapper organizationMapper;

    private final LocationMapper locationMapper;

    @Inject
    public UserMapper(DocumentMapper documentMapper, OrganizationMapper organizationMapper,
                      LocationMapper locationMapper) {
        this.documentMapper = documentMapper;
        this.organizationMapper = organizationMapper;
        this.locationMapper = locationMapper;
    }

    @Override
    public UserRepresentation apply(User user) {
        if (user == null) {
            return null;
        }

        List<Pair<Scope, Role>> permissions = user.getPermissions();

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
            .setScopes(
                permissions.stream().map(Pair::getKey).distinct().collect(toList()))
            .setPostAuthor(
                permissions.stream().map(Pair::getValue).anyMatch(NON_MEMBER_ROLES::contains))
            .setDefaultOrganization(organizationMapper.apply(user.getDefaultOrganization()))
            .setDefaultLocation(locationMapper.apply(user.getDefaultLocation()))
            .setRegistered(user.isRegistered());
    }

}
