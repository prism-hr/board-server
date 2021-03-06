package hr.prism.board.service;

import hr.prism.board.authentication.AuthenticationToken;
import hr.prism.board.dao.UserDAO;
import hr.prism.board.domain.*;
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
import static hr.prism.board.enums.Role.ADMINISTRATOR;
import static hr.prism.board.enums.Scope.DEPARTMENT;
import static hr.prism.board.enums.State.ACCEPTED_STATES;
import static hr.prism.board.exception.ExceptionCode.*;
import static hr.prism.board.utils.BoardUtils.isPresent;
import static java.util.UUID.randomUUID;
import static org.apache.commons.codec.digest.DigestUtils.sha256Hex;

@Service
@Transactional
public class UserService {

    private static final String TEST_USER_SUFFIX = "@test.prism.hr";

    private final Long passwordResetTimeoutSeconds;

    private final UserRepository userRepository;

    private final UserDAO userDAO;

    private final UserPatchService userPatchService;

    private final LocationService locationService;

    @Inject
    public UserService(@Value("${password.reset.timeout.seconds}") Long passwordResetTimeoutSeconds,
                       UserRepository userRepository, UserDAO userDAO, UserPatchService userPatchService,
                       LocationService locationService) {
        this.passwordResetTimeoutSeconds = passwordResetTimeoutSeconds;
        this.userRepository = userRepository;
        this.userDAO = userDAO;
        this.userPatchService = userPatchService;
        this.locationService = locationService;
    }

    public User getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication instanceof AuthenticationToken ? ((AuthenticationToken) authentication).getUser() : null;
    }

    public User getById(Long id) {
        User user = userRepository.findOne(id);
        if (user == null) {
            return null;
        }

        for (UserRole userRole : user.getUserRoles()) {
            Role role = userRole.getRole();
            if (role == ADMINISTRATOR) {
                user.setDepartmentAdministrator(DEPARTMENT == userRole.getResource().getScope());
                user.setPostCreator(true);
            }

            if (user.isDepartmentAdministrator() && user.isPostCreator()) {
                return user;
            }
        }

        return user;
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

    public List<User> getByUserRoleWithoutUserRole(Resource resource, Role role, Resource withoutResource,
                                                   Role withoutRole) {
        return userRepository.findByRoleWithoutRole(resource, role, withoutResource, withoutRole);
    }

    public List<Long> getByResourceAndUserIds(Resource resource, List<Long> userIds) {
        return userRepository.findByResourceAndUserIds(resource, userIds, ACCEPTED_STATES);
    }

    public List<UserNotification> getByResourceAndEnclosingRole(Resource resource, Scope enclosingScope, Role role) {
        return userRepository.findByResourceAndEnclosingScopeAndRole(
            resource, enclosingScope, role, ACCEPTED_STATES, LocalDate.now());
    }

    public List<UserNotification> getByResourceAndEnclosingRoleCategorized(Resource resource, Scope enclosingScope,
                                                                           Role role) {
        return userRepository.findByResourceAndEnclosingScopeAndRoleAndCategory(
            resource, enclosingScope, role, ACCEPTED_STATES, MEMBER, LocalDate.now());
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

    public User updateUser(User user, UserPatchDTO userDTO) {
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

        userPatchService.patchDocument(user,
            user::getDocumentImage, user::setDocumentImage, userDTO.getDocumentImage());
        userPatchService.patchProperty(user, user::getDocumentImageRequestState, user::setDocumentImageRequestState,
            userDTO.getDocumentImageRequestState());
        userPatchService.patchProperty(user,
            user::getSeenWalkThrough, user::setSeenWalkThrough, userDTO.getSeenWalkThrough());
        userPatchService.patchProperty(user, user::getGender, user::setGender, userDTO.getGender());
        userPatchService.patchProperty(user, user::getAgeRange, user::setAgeRange, userDTO.getAgeRange());
        userPatchService.patchLocation(user,
            user::getLocationNationality, user::setLocationNationality, userDTO.getLocationNationality());
        userPatchService.patchDocument(user,
            user::getDocumentResume, user::setDocumentResume, userDTO.getDocumentResume());
        userPatchService.patchProperty(user,
            user::getWebsiteResume, user::setWebsiteResume, userDTO.getWebsiteResume());

        user.setIndexData();
        return user;
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

    public void updateUserOrganizationAndLocation(User user, Organization organization, Location location) {
        user.setDefaultOrganization(organization);
        user.setDefaultLocation(location);
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
        user.setIndexData();

        user.setTestUser(user.getEmail().endsWith(TEST_USER_SUFFIX));
        updateUserMembership(user, userDTO);
        return saveUser(user);
    }

    public interface UserFinder {
        User getByEmail(String email);
    }

}
