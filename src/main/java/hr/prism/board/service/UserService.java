package hr.prism.board.service;

import hr.prism.board.authentication.AuthenticationToken;
import hr.prism.board.dao.UserDAO;
import hr.prism.board.domain.Document;
import hr.prism.board.domain.Resource;
import hr.prism.board.domain.User;
import hr.prism.board.dto.*;
import hr.prism.board.enums.*;
import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.BoardForbiddenException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.repository.UserRepository;
import hr.prism.board.value.UserNotification;
import hr.prism.board.value.UserSearch;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static hr.prism.board.enums.CategoryType.MEMBER;
import static hr.prism.board.enums.DocumentRequestState.DISPLAY_FIRST;
import static hr.prism.board.enums.PasswordHash.SHA256;
import static hr.prism.board.enums.State.ACTIVE_USER_ROLE_STATES;
import static hr.prism.board.exception.ExceptionCode.*;
import static hr.prism.board.utils.BoardUtils.isPresent;
import static hr.prism.board.utils.BoardUtils.makeSoundex;
import static java.util.UUID.randomUUID;
import static org.apache.commons.codec.digest.DigestUtils.sha256Hex;

@Service
@Transactional
public class UserService {

    private static final String TEST_USER_SUFFIX = "@test.prism.hr";

    private final Long passwordResetTimeoutSeconds;

    private final UserRepository userRepository;

    private final UserPatchService userPatchService;

    private final UserDAO userDAO;

    private final LocationService locationService;

    @Inject
    public UserService(@Value("${password.reset.timeout.seconds}") Long passwordResetTimeoutSeconds,
                       UserRepository userRepository, UserPatchService userPatchService, UserDAO userDAO,
                       LocationService locationService) {
        this.passwordResetTimeoutSeconds = passwordResetTimeoutSeconds;
        this.userRepository = userRepository;
        this.userPatchService = userPatchService;
        this.userDAO = userDAO;
        this.locationService = locationService;
    }

    public User getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null ? null : ((AuthenticationToken) authentication).getUser();
    }

    public User getUserSecured() {
        User user = getUser();
        if (user == null) {
            throw new BoardForbiddenException(UNAUTHENTICATED_USER, "User cannot be authenticated");
        }

        return user;
    }

    public User getById(Long id) {
        return userRepository.findOne(id);
    }

    public User getByUuid(String uuid) {
        return userRepository.findByUuid(uuid);
    }

    public User getByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User getByOauthCredentials(OauthProvider provider, String oauthAccountId) {
        return userRepository.findByOauthProviderAndOauthAccountId(provider, oauthAccountId);
    }

    public User getByUserRoleUuid(String uuid) {
        User user = userRepository.findByUserRoleUuid(uuid);
        if (user == null) {
            throw new BoardForbiddenException(UNKNOWN_USER, "User with user role uuid: " + uuid + " cannot be found");
        }

        return user;
    }

    public User getByEmail(Resource resource, String email, Role role) {
        List<User> potentialUsers = userRepository.findByEmail(resource, email, role);
        if (potentialUsers.isEmpty()) {
            return null;
        }

        return potentialUsers.stream()
            .filter(potentialUser -> potentialUser.getEmail().equals(email))
            .findFirst()
            .orElse(potentialUsers.get(0));
    }

    public List<Long> getByResourceAndEvents(Resource resource, List<ResourceEvent> events) {
        return userRepository.findByResourceAndEvents(resource, events);
    }

    public List<User> getByUserRoleWithoutUserRole(Resource resource, Role role, Resource withoutResource,
                                                   Role withoutRole) {
        return userRepository.findByRoleWithoutRole(resource, role, withoutResource, withoutRole);
    }

    public List<Long> getByResourceAndUserIds(Resource resource, List<Long> userIds) {
        return userRepository.findByResourceAndUserIds(resource, userIds, ACTIVE_USER_ROLE_STATES);
    }

    public List<UserNotification> getByResourceAndEnclosingRole(Resource resource, Scope enclosingScope, Role role) {
        return userRepository.findByResourceAndEnclosingScopeAndRole(
            resource, enclosingScope, role, ACTIVE_USER_ROLE_STATES, LocalDate.now());
    }

    public List<UserNotification> getByResourceAndEnclosingRoleCategorized(Resource resource, Scope enclosingScope,
                                                                           Role role) {
        return userRepository.findByResourceAndEnclosingScopeAndRoleAndCategory(
            resource, enclosingScope, role, ACTIVE_USER_ROLE_STATES, MEMBER, LocalDate.now());
    }


    public List<UserSearch> findUsers(String searchTerm) {
        return userDAO.findUsers(searchTerm);
    }

    public User createUser(RegisterDTO registerDTO) {
        User user = new User()
            .setUuid(randomUUID().toString())
            .setGivenName(registerDTO.getGivenName())
            .setSurname(registerDTO.getSurname())
            .setEmail(registerDTO.getEmail())
            .setPassword(sha256Hex(registerDTO.getPassword()))
            .setPasswordHash(SHA256)
            .setDocumentImageRequestState(DISPLAY_FIRST);
        return saveUser(user);
    }

    public User createOrUpdateUser(UserDTO userDTO, UserFinder userFinder) {
        Long userId = userDTO.getId();
        if (userId != null) {
            User user = userRepository.findOne(userId);
            return updateUserMembership(user, userDTO);
        }

        String email = userDTO.getEmail();
        if (email != null) {
            User user = userFinder.getByEmail(email);
            return updateUserMembership(user, userDTO);
        }

        return createUser(userDTO);
    }

    @SuppressWarnings("UnusedReturnValue")
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    public User updateUserIndex(User user) {
        user.setIndexData(makeSoundex(user.getGivenName(), user.getSurname()));
        return userRepository.save(user);
    }

    public User updateUser(UserPatchDTO userDTO) {
        User user = getUserSecured();
        userPatchService.patchProperty(user, user::getGivenName, user::setGivenName, userDTO.getGivenName());
        userPatchService.patchProperty(user, user::getSurname, user::setSurname, userDTO.getSurname());

        Optional<String> emailOptional = userDTO.getEmail();
        if (isPresent(emailOptional)) {
            @SuppressWarnings("ConstantConditions")
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

        userPatchService.patchDocument(user, user::getDocumentImage, user::setDocumentImage, userDTO.getDocumentImage());
        userPatchService.patchProperty(user, user::getDocumentImageRequestState, user::setDocumentImageRequestState, userDTO.getDocumentImageRequestState());
        userPatchService.patchProperty(user, user::getSeenWalkThrough, user::setSeenWalkThrough, userDTO.getSeenWalkThrough());
        userPatchService.patchProperty(user, user::getGender, user::setGender, userDTO.getGender());
        userPatchService.patchProperty(user, user::getAgeRange, user::setAgeRange, userDTO.getAgeRange());
        userPatchService.patchLocation(user, user::getLocationNationality, user::setLocationNationality, userDTO.getLocationNationality());
        userPatchService.patchDocument(user, user::getDocumentResume, user::setDocumentResume, userDTO.getDocumentResume());
        userPatchService.patchProperty(user, user::getWebsiteResume, user::setWebsiteResume, userDTO.getWebsiteResume());
        return updateUserIndex(user);
    }

    public User updateUserMembership(User user, UserDTO userDTO) {
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

        return userRepository.save(user);
    }

    public void updateUserResume(User user, Document documentResume, String websiteResume) {
        user.setDocumentResume(documentResume);
        user.setWebsiteResume(websiteResume);
        userRepository.save(user);
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

    public User saveUser(User user) {
        user = userRepository.save(user);
        user.setCreatorId(user.getId());
        return user;
    }

    public void deleteUser(User user) {
        userRepository.delete(user);
    }

    private User createUser(UserDTO userDTO) {
        User user = new User();
        user.setUuid(randomUUID().toString());

        user.setGivenName(userDTO.getGivenName());
        user.setSurname(userDTO.getSurname());

        user.setEmail(userDTO.getEmail());
        user.setIndexData(makeSoundex(user.getGivenName(), user.getSurname()));

        user.setTestUser(user.getEmail().endsWith(TEST_USER_SUFFIX));
        updateUserMembership(user, userDTO);
        return saveUser(user);
    }

    public interface UserFinder {
        User getByEmail(String email);
    }

}
