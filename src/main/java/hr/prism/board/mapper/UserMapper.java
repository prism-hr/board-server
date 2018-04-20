package hr.prism.board.mapper;

import hr.prism.board.domain.User;
import hr.prism.board.enums.Role;
import hr.prism.board.enums.Scope;
import hr.prism.board.representation.DocumentRepresentation;
import hr.prism.board.representation.UserRepresentation;
import hr.prism.board.value.UserSearch;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;
import java.util.function.Function;

import static hr.prism.board.enums.Role.STAFF_ROLES;
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
                permissions.stream().map(Pair::getValue).anyMatch(STAFF_ROLES::contains))
            .setDefaultOrganization(organizationMapper.apply(user.getDefaultOrganization()))
            .setDefaultLocation(locationMapper.apply(user.getDefaultLocation()))
            .setRegistered(user.isRegistered());
    }

    public UserRepresentation apply(UserSearch user) {
        if (user == null) {
            return null;
        }

        DocumentRepresentation documentImage = null;
        String documentImageCloudinaryId = user.getDocumentImageCloudinaryId();
        if (documentImageCloudinaryId != null) {
            documentImage =
                new DocumentRepresentation()
                    .setCloudinaryId(documentImageCloudinaryId)
                    .setCloudinaryUrl(user.getDocumentImageCloudinaryUrl())
                    .setFileName(user.getDocumentImageFileName());
        }

        return new UserRepresentation()
            .setId(user.getId())
            .setGivenName(user.getGivenName())
            .setSurname(user.getSurname())
            .setEmail(user.getEmailDisplay())
            .setDocumentImage(documentImage);
    }

}
