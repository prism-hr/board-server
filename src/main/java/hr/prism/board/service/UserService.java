package hr.prism.board.service;

import hr.prism.board.authentication.AuthenticationToken;
import hr.prism.board.domain.Document;
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
import hr.prism.board.service.cache.UserCacheService;
import hr.prism.board.util.BoardUtils;
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
public class UserService {

    private static final String USER_SEARCH_STATEMENT =
        "SELECT user.id, user.given_name, user.surname, user.email, document_image.cloudinary_id, document_image.cloudinary_url, document_image.file_name " +
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

    public User findByUuid(String uuid) {
        return userRepository.findByUuid(uuid);
    }

    public User updateUser(UserPatchDTO userDTO) {
        User user = getCurrentUserSecured(true);
        userPatchService.patchProperty(user, user::getGivenName, user::setGivenName, userDTO.getGivenName());
        userPatchService.patchProperty(user, user::getSurname, user::setSurname, userDTO.getSurname());

        // TODO: remember the original email as key for uploads
        Optional<String> emailOptional = userDTO.getEmail();
        if (emailOptional != null) {
            if (emailOptional.isPresent()) {
                String email = emailOptional.get();
                User duplicateUser = userRepository.findByEmailAndNotId(email, user.getId());
                if (duplicateUser == null) {
                    user.setEmail(email);
                } else {
                    throw new BoardException(ExceptionCode.DUPLICATE_USER);
                }
            } else {
                throw new BoardException(ExceptionCode.MISSING_USER_EMAIL);
            }
        }

        userPatchService.patchDocument(user, user::getDocumentImage, user::setDocumentImage, userDTO.getDocumentImage());
        userPatchService.patchProperty(user, user::getDocumentImageRequestState, user::setDocumentImageRequestState, userDTO.getDocumentImageRequestState());
        userPatchService.patchDocument(user, user::getDocumentResume, user::setDocumentResume, userDTO.getDocumentResume());
        userPatchService.patchProperty(user, user::getWebsiteResume, user::setWebsiteResume, userDTO.getWebsiteResume());
        return userCacheService.updateUser(user);
    }

    public void updateUserResume(User user, Document documentResume, String websiteResume) {
        user.setDocumentResume(documentResume);
        user.setWebsiteResume(websiteResume);
    }

    public void resetPassword(UserPasswordDTO userPasswordDTO) {
        String uuid = userPasswordDTO.getUuid();
        User user = userRepository.findByPasswordResetUuid(uuid);
        if (user == null) {
            throw new BoardForbiddenException(ExceptionCode.FORBIDDEN_PASSWORD_RESET);
        }

        LocalDateTime baseline = LocalDateTime.now();
        if (user.getPasswordResetTimestamp().plusSeconds(passwordResetTimeoutSeconds).isBefore(baseline)) {
            throw new BoardForbiddenException(ExceptionCode.FORBIDDEN_PASSWORD_RESET);
        }

        user.setPassword(DigestUtils.sha256Hex(userPasswordDTO.getPassword()));
        user.setPasswordResetUuid(null);
        user.setPasswordResetTimestamp(null);
    }

    public User getOrCreateUser(UserDTO userDTO) {
        User user = null;
        if (userDTO.getId() != null) {
            user = userRepository.findOne(userDTO.getId());
        }

        if (user == null) {
            user = userRepository.findByEmail(userDTO.getEmail());
        }

        if (user == null) {
            user = new User();
            user.setUuid(UUID.randomUUID().toString());
            user.setGivenName(userDTO.getGivenName());
            user.setSurname(userDTO.getSurname());
            user.setEmail(userDTO.getEmail());
            user = userRepository.save(user);
            user.setIndexData(BoardUtils.makeSoundexRemovingStopWords(user.getGivenName(), user.getSurname(), user.getEmail()));
            return user;
        }

        return user;
    }

    public List<Long> findByResourceAndUserIds(Resource resource, Collection<Long> userIds) {
        return userRepository.findByResourceAndUserIds(resource, userIds, State.ACTIVE_USER_ROLE_STATES);
    }

    public List<User> findByResourceAndEnclosingScopeAndRole(Resource resource, Scope enclosingScope, Role role) {
        return userRepository.findByResourceAndEnclosingScopeAndRole(resource, enclosingScope, role, State.ACTIVE_USER_ROLE_STATES, LocalDate.now());
    }

    public List<User> findByResourceAndEnclosingScopeAndRoleAndCategories(Resource resource, Scope enclosingScope, Role role) {
        return userRepository.findByResourceAndEnclosingScopeAndRoleAndCategories(resource, enclosingScope, role, State.ACTIVE_USER_ROLE_STATES, CategoryType.MEMBER, LocalDate.now());
    }

    public List<User> findByRoleWithoutRole(Resource resource, Role role, Resource withoutResource, Role withoutRole) {
        return userRepository.findByRoleWithoutRole(resource, role, withoutResource, withoutRole);
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
            userRepresentation.setEmail(BoardUtils.obfuscateEmail(result[3].toString()));

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

    public User getCurrentUser(boolean fresh) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }

        if (fresh) {
            return userCacheService.findOneFresh(((AuthenticationToken) authentication).getUserId());
        }

        return userCacheService.findOne(((AuthenticationToken) authentication).getUserId());
    }

    public User getCurrentUserSecured(boolean fresh) {
        User user = getCurrentUser(fresh);
        if (user == null) {
            throw new BoardForbiddenException(ExceptionCode.UNAUTHENTICATED_USER);
        }

        return user;
    }

    public List<Long> findByResourceAndRoleAndStates(Resource resource, Role role, List<State> states) {
        return userRepository.findByResourceAndRoleAndStates(resource, role, states);
    }

    public List<Long> findByResourceAndEvent(Resource resource, ResourceEvent event) {
        return userRepository.findByResourceAndEvent(resource, event);
    }

    public void createSearchResults(String search, String searchTerm, Collection<Long> userIds) {
        userSearchRepository.insertBySearch(search, BoardUtils.makeSoundexRemovingStopWords(searchTerm), userIds);
    }

    public void deleteSearchResults(String search) {
        userSearchRepository.deleteBySearch(search);
    }

    public Long findUserCount(Resource resource, Role role, List<String> emails) {
        return userRepository.findUserCount(resource, role, State.ACTIVE_USER_ROLE_STATES, emails);
    }

}
