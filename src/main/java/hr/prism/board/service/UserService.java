package hr.prism.board.service;

import com.pusher.rest.Pusher;
import hr.prism.board.authentication.AuthenticationToken;
import hr.prism.board.domain.Document;
import hr.prism.board.domain.Post;
import hr.prism.board.domain.Resource;
import hr.prism.board.domain.User;
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
import hr.prism.board.utils.BoardUtils;
import hr.prism.board.value.UserNotification;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static hr.prism.board.exception.ExceptionCode.*;

@Service
@Transactional
@SuppressWarnings({"SpringAutowiredFieldsWarningInspection", "SqlResolve", "WeakerAccess"})
public class UserService {

    private static Logger LOGGER = LoggerFactory.getLogger(UserService.class);

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
    private UserPatchService userPatchService;

    @Inject
    private LocationService locationService;

    @Inject
    private UserRoleService userRoleService;

    @Inject
    private PostService postService;

    @Inject
    private EntityManager entityManager;

    @Inject
    private Pusher pusher;

    @Value("${password.reset.timeout.seconds}")
    private Long passwordResetTimeoutSeconds;

    public User getCurrentUser() {
        return getCurrentUser(false);
    }

    public User getCurrentUserSecured() {
        return getCurrentUserSecured(false);
    }

    public User getUserForRepresentation() {
        User user = getCurrentUserSecured()
            .setRevealEmail(true);

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
                    throw new BoardException(DUPLICATE_USER, "Email address already in use");
                }
            } else {
                throw new BoardException(MISSING_USER_EMAIL, "Cannot unset email address");
            }
        }

        userPatchService.patchDocument(user, user::getDocumentImage, user::setDocumentImage, userDTO.getDocumentImage());
        userPatchService.patchProperty(user, user::getDocumentImageRequestState, user::setDocumentImageRequestState, userDTO.getDocumentImageRequestState());
        userPatchService.patchProperty(user, user::getSeenWalkThrough, user::setSeenWalkThrough, userDTO.getSeenWalkThrough());
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

    public List<UserNotification> findByResourceAndEnclosingScopeAndRole(Resource resource, Scope enclosingScope, Role role) {
        return userRepository.findByResourceAndEnclosingScopeAndRole(resource, enclosingScope, role, State.ACTIVE_USER_ROLE_STATES, LocalDate.now());
    }

    public List<UserNotification> findByResourceAndEnclosingScopeAndRoleAndCategories(Resource resource, Scope enclosingScope, Role role) {
        return userRepository.findByResourceAndEnclosingScopeAndRoleAndCategory(resource, enclosingScope, role, State.ACTIVE_USER_ROLE_STATES, CategoryType.MEMBER, LocalDate
            .now());
    }

    public void deleteTestUsers() {
        List<Long> userIds = userRepository.findByTestUser(true);
        if (!userIds.isEmpty()) {
            Query removeForeignKeyChecks = entityManager.createNativeQuery("SET SESSION FOREIGN_KEY_CHECKS = 0");
            removeForeignKeyChecks.executeUpdate();

            @SuppressWarnings("unchecked")
            List<String> tablesNames = entityManager.createNativeQuery("SHOW TABLES").getResultList();
            tablesNames.stream().filter(tableName -> !Arrays.asList("schema_version", "workflow").contains(tableName)).forEach(tableName -> {
                Query deleteUserData = entityManager.createNativeQuery("DELETE FROM " + tableName + " WHERE creator_id IN (:userIds)");
                deleteUserData.setParameter("userIds", userIds);
                deleteUserData.executeUpdate();
            });

            Query restoreForeignKeyChecks = entityManager.createNativeQuery("SET SESSION FOREIGN_KEY_CHECKS = 1");
            restoreForeignKeyChecks.executeUpdate();
        }
    }

    public List<Long> findByResourceAndUserIds(Resource resource, List<Long> userIds) {
        return userRepository.findByResourceAndUserIds(resource, userIds, State.ACTIVE_USER_ROLE_STATES);
    }

    public User findByUuid(String uuid) {
        return userRepository.findByUuid(uuid);
    }

    public void updateUserResume(User user, Document documentResume, String websiteResume) {
        user.setDocumentResume(documentResume);
        user.setWebsiteResume(websiteResume);
    }

    public List<User> findByRoleWithoutRole(Resource resource, Role role, Resource withoutResource, Role withoutRole) {
        return userRepository.findByRoleWithoutRole(resource, role, withoutResource, withoutRole);
    }

    public User getCurrentUserSecured(boolean fresh) {
        User user = getCurrentUser(fresh);
        if (user == null) {
            throw new BoardForbiddenException(UNAUTHENTICATED_USER, "User cannot be authenticated");
        }

        return user;
    }

    private User getCurrentUser(boolean fresh) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }

        if (fresh) {
            return userCacheService.getUserFromDatabase(((AuthenticationToken) authentication).getUser());
        }

        return userCacheService.getUser(((AuthenticationToken) authentication).getUser());
    }

    public interface UserFinder {
        User getByEmail(String email);
    }

}
