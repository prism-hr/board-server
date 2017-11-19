package hr.prism.board.service;

import hr.prism.board.authentication.AuthenticationToken;
import hr.prism.board.domain.Document;
import hr.prism.board.domain.Post;
import hr.prism.board.domain.Resource;
import hr.prism.board.domain.User;
import hr.prism.board.dto.LocationDTO;
import hr.prism.board.dto.UserDTO;
import hr.prism.board.dto.UserPasswordDTO;
import hr.prism.board.dto.UserPatchDTO;
import hr.prism.board.enums.*;
import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.BoardForbiddenException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.repository.UserRepository;
import hr.prism.board.repository.UserSearchRepository;
import hr.prism.board.representation.DocumentRepresentation;
import hr.prism.board.representation.UserRepresentation;
import hr.prism.board.service.cache.UserCacheService;
import hr.prism.board.utils.BoardUtils;
import hr.prism.board.value.UserNotification;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
@SuppressWarnings("SpringAutowiredFieldsWarningInspection")
public class UserService {

    @SuppressWarnings("SqlResolve")
    private static final String USER_SEARCH_STATEMENT =
        "SELECT user.id, user.given_name, user.surname, user.email_display, document_image.cloudinary_id, document_image.cloudinary_url, document_image.file_name " +
            "FROM user " +
            "LEFT JOIN document AS document_image " +
            "ON user.document_image_id = document_image.id " +
            "WHERE user.given_name LIKE :searchTerm " +
            "OR user.surname LIKE :searchTerm " +
            "OR CONCAT(user.given_name, ' ', user.surname) LIKE :searchTerm " +
            "OR user.email LIKE :searchTerm " +
            "ORDER BY user.given_name, user.surname, user.email " +
            "LIMIT 10";

    @Inject
    private UserRepository userRepository;

    @Inject
    private UserSearchRepository userSearchRepository;

    @Inject
    private UserCacheService userCacheService;

    @Inject
    private ResourceService resourceService;

    @Inject
    private ActionService actionService;

    @Inject
    private UserPatchService userPatchService;

    @Inject
    private LocationService locationService;

    @Inject
    private UserRoleService userRoleService;

    @Inject
    private PostService postService;

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    @SuppressWarnings("SpringJavaAutowiringInspection")
    private PlatformTransactionManager platformTransactionManager;

    @Value("${password.reset.timeout.seconds}")
    private Long passwordResetTimeoutSeconds;

    public User getCurrentUser() {
        return getCurrentUser(false);
    }

    public User getCurrentUserSecured() {
        return getCurrentUserSecured(false);
    }

    public User getUserForRepresentation() {
        User user = getCurrentUserSecured().setRevealEmail(true);
        if (!userRoleService.hasAdministratorRole(user)) {
            // Assume user is usually posting - should have default values for organization
            Post latestPost = postService.findLatestPost(user);
            if (latestPost != null) {
                user.setDefaultOrganizationName(latestPost.getOrganizationName());
                user.setDefaultLocation(latestPost.getLocation());
            }
        }

        return user;
    }

    public User updateUser(UserPatchDTO userDTO) {
        User user = getCurrentUserSecured(true);
        userPatchService.patchProperty(user, user::getGivenName, user::setGivenName, userDTO.getGivenName());
        userPatchService.patchProperty(user, user::getSurname, user::setSurname, userDTO.getSurname());

        Optional<String> emailOptional = userDTO.getEmail();
        if (emailOptional != null) {
            if (emailOptional.isPresent()) {
                String email = emailOptional.get();
                User duplicateUser = userRepository.findByEmailAndNotId(email, user.getId());
                if (duplicateUser == null) {
                    user.setEmail(email);
                } else {
                    throw new BoardException(ExceptionCode.DUPLICATE_USER, "Email address already in use");
                }
            } else {
                throw new BoardException(ExceptionCode.MISSING_USER_EMAIL, "Cannot unset email address");
            }
        }

        userPatchService.patchDocument(user, user::getDocumentImage, user::setDocumentImage, userDTO.getDocumentImage());
        userPatchService.patchProperty(user, user::getDocumentImageRequestState, user::setDocumentImageRequestState, userDTO.getDocumentImageRequestState());
        userPatchService.patchProperty(user, user::getGender, user::setGender, userDTO.getGender());
        userPatchService.patchProperty(user, user::getAgeRange, user::setAgeRange, userDTO.getAgeRange());
        userPatchService.patchLocation(user, user::getLocationNationality, user::setLocationNationality, userDTO.getLocationNationality());
        userPatchService.patchDocument(user, user::getDocumentResume, user::setDocumentResume, userDTO.getDocumentResume());
        userPatchService.patchProperty(user, user::getWebsiteResume, user::setWebsiteResume, userDTO.getWebsiteResume());
        return userCacheService.updateUser(user);
    }

    public void resetPassword(UserPasswordDTO userPasswordDTO) {
        String uuid = userPasswordDTO.getUuid();
        User user = userRepository.findByPasswordResetUuid(uuid);
        if (user == null) {
            throw new BoardForbiddenException(ExceptionCode.FORBIDDEN_PASSWORD_RESET, "Invalid password reset token");
        }

        LocalDateTime baseline = LocalDateTime.now();
        if (user.getPasswordResetTimestamp().plusSeconds(passwordResetTimeoutSeconds).isBefore(baseline)) {
            throw new BoardForbiddenException(ExceptionCode.FORBIDDEN_PASSWORD_RESET, "Expired password reset token");
        }

        user.setPassword(DigestUtils.sha256Hex(userPasswordDTO.getPassword()));
        user.setPasswordHash(PasswordHash.SHA256);
        user.setPasswordResetUuid(null);
        user.setPasswordResetTimestamp(null);
    }

    public List<Long> findByResourceAndUserIds(Resource resource, List<Long> userIds) {
        return userRepository.findByResourceAndUserIds(resource, userIds, State.ACTIVE_USER_ROLE_STATES);
    }

    public List<UserNotification> findByResourceAndEnclosingScopeAndRole(Resource resource, Scope enclosingScope, Role role) {
        return userRepository.findByResourceAndEnclosingScopeAndRole(resource, enclosingScope, role, State.ACTIVE_USER_ROLE_STATES, LocalDate.now());
    }

    public List<UserNotification> findByResourceAndEnclosingScopeAndRoleAndCategories(Resource resource, Scope enclosingScope, Role role) {
        return userRepository.findByResourceAndEnclosingScopeAndRoleAndCategory(resource, enclosingScope, role, State.ACTIVE_USER_ROLE_STATES, CategoryType.MEMBER, LocalDate.now());
    }

    @SuppressWarnings("unchecked")
    public List<UserRepresentation> findBySimilarNameAndEmail(Scope scope, Long resourceId, String searchTerm) {
        // Apply security to the lookup request
        User currentUser = getCurrentUserSecured();
        Resource resource = resourceService.getResource(currentUser, scope, resourceId);
        actionService.executeAction(currentUser, resource, Action.EDIT, () -> resource);

        List<Object[]> results = new TransactionTemplate(platformTransactionManager).execute(status ->
            entityManager.createNativeQuery(USER_SEARCH_STATEMENT)
                .setParameter("searchTerm", searchTerm + "%")
                .getResultList());

        List<UserRepresentation> userRepresentations = new ArrayList<>();
        for (Object[] result : results) {
            UserRepresentation userRepresentation = new UserRepresentation();
            userRepresentation.setId(Long.parseLong(result[0].toString()));
            userRepresentation.setGivenName(result[1].toString());
            userRepresentation.setSurname(result[2].toString());
            userRepresentation.setEmail(result[3].toString());

            Object cloudinaryId = result[4];
            if (cloudinaryId != null) {
                DocumentRepresentation documentRepresentation = new DocumentRepresentation();
                documentRepresentation.setCloudinaryId(cloudinaryId.toString());
                documentRepresentation.setCloudinaryUrl(result[5].toString());
                documentRepresentation.setFileName(result[6].toString());
                userRepresentation.setDocumentImage(documentRepresentation);
            }

            userRepresentations.add(userRepresentation);
        }

        return userRepresentations;
    }

    User findByUuid(String uuid) {
        return userRepository.findByUuid(uuid);
    }

    void updateUserDemographicData(User user, UserDTO userDTO) {
        Gender gender = userDTO.getGender();
        if (gender != null) {
            user.setGender(gender);
        }

        AgeRange ageRange = userDTO.getAgeRange();
        if (ageRange != null) {
            user.setAgeRange(ageRange);
        }

        LocationDTO locationNationality = userDTO.getLocationNationality();
        if (locationNationality != null) {
            user.setLocationNationality(locationService.getOrCreateLocation(locationNationality));
        }
    }

    void updateUserResume(User user, Document documentResume, String websiteResume) {
        user.setDocumentResume(documentResume);
        user.setWebsiteResume(websiteResume);
    }

    User getOrCreateUser(UserDTO userDTO, UserFinder userFinder) {
        User user = null;
        if (userDTO.getId() != null) {
            user = userRepository.findOne(userDTO.getId());
        }

        String email = userDTO.getEmail();
        if (user == null && email != null) {
            user = userFinder.getByEmail(email);
        }

        if (user == null) {
            user = new User();
            user.setUuid(UUID.randomUUID().toString());
            user.setGivenName(userDTO.getGivenName());
            user.setSurname(userDTO.getSurname());
            user.setEmail(userDTO.getEmail());
            user = userRepository.save(user);
            userCacheService.setIndexData(user);
            return user;
        }

        return user;
    }


    List<User> findByRoleWithoutRole(Resource resource, Role role, Resource withoutResource, Role withoutRole) {
        return userRepository.findByRoleWithoutRole(resource, role, withoutResource, withoutRole);
    }

    User getCurrentUserSecured(boolean fresh) {
        User user = getCurrentUser(fresh);
        if (user == null) {
            throw new BoardForbiddenException(ExceptionCode.UNAUTHENTICATED_USER, "User cannot be authenticated");
        }

        return user;
    }

    List<Long> findByResourceAndRoleAndStates(Resource resource, List<Role> roles, State state) {
        return userRepository.findByResourceAndRolesAndState(resource, roles, state);
    }

    List<Long> findByResourceAndEvent(Resource resource, ResourceEvent event) {
        return userRepository.findByResourceAndEvent(resource, event);
    }

    List<Long> findByResourceAndEvents(Resource resource, List<ResourceEvent> events) {
        return userRepository.findByResourceAndEvents(resource, events);
    }

    void createSearchResults(String search, String searchTerm, Collection<Long> userIds) {
        userSearchRepository.insertBySearch(search, LocalDateTime.now(), BoardUtils.makeSoundex(searchTerm), userIds);
    }

    void deleteSearchResults(String search) {
        userSearchRepository.deleteBySearch(search);
    }

    private User getCurrentUser(boolean fresh) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }

        if (fresh) {
            return userCacheService.findOneFresh(((AuthenticationToken) authentication).getUserId());
        }

        return userCacheService.findOne(((AuthenticationToken) authentication).getUserId());
    }

    public interface UserFinder {
        User getByEmail(String email);
    }

}
