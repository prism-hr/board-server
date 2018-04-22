package hr.prism.board.service;

import hr.prism.board.authentication.AuthenticationToken;
import hr.prism.board.dao.UserDAO;
import hr.prism.board.domain.Document;
import hr.prism.board.domain.Resource;
import hr.prism.board.domain.User;
import hr.prism.board.dto.LocationDTO;
import hr.prism.board.dto.RegisterDTO;
import hr.prism.board.dto.UserDTO;
import hr.prism.board.enums.*;
import hr.prism.board.exception.BoardForbiddenException;
import hr.prism.board.repository.UserRepository;
import hr.prism.board.value.UserSearch;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;

import static hr.prism.board.enums.DocumentRequestState.DISPLAY_FIRST;
import static hr.prism.board.enums.PasswordHash.SHA256;
import static hr.prism.board.exception.ExceptionCode.UNAUTHENTICATED_USER;
import static hr.prism.board.exception.ExceptionCode.UNKNOWN_USER;
import static hr.prism.board.utils.BoardUtils.makeSoundex;
import static java.util.UUID.randomUUID;
import static org.apache.commons.codec.digest.DigestUtils.sha256Hex;

@Service
@Transactional
public class NewUserService {

    private static final String TEST_USER_SUFFIX = "@test.prism.hr";

    private final UserRepository userRepository;

    private final UserDAO userDAO;

    private final LocationService locationService;

    @Inject
    public NewUserService(UserRepository userRepository, UserDAO userDAO, LocationService locationService) {
        this.userRepository = userRepository;
        this.userDAO = userDAO;
        this.locationService = locationService;
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

    public List<User> getUsersWithRoleWithoutRole(Resource resource, Role role, Resource withoutResource,
                                                  Role withoutRole) {
        return userRepository.findByRoleWithoutRole(resource, role, withoutResource, withoutRole);
    }

    public User createOrUpdateUser(UserDTO userDTO, UserFinder userFinder) {
        Long userId = userDTO.getId();
        if (userId != null) {
            User user = userRepository.findOne(userId);
            return updateMembership(user, userDTO);
        }

        String email = userDTO.getEmail();
        if (email != null) {
            User user = userFinder.getByEmail(email);
            return updateMembership(user, userDTO);
        }

        return createUser(userDTO);
    }

    public User updateUser(User user) {
        return userRepository.update(user);
    }

    public User indexAndUpdateUser(User user) {
        user.setIndexData(makeSoundex(user.getGivenName(), user.getSurname()));
        return userRepository.update(user);
    }

    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null ? null : ((AuthenticationToken) authentication).getUser();
    }

    public User requireAuthenticatedUser() {
        User user = getAuthenticatedUser();
        if (user == null) {
            throw new BoardForbiddenException(UNAUTHENTICATED_USER, "User cannot be authenticated");
        }

        return user;
    }

    public User updateMembership(User user, UserDTO userDTO) {
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

        return user;
    }

    public void updateUserResume(User user, Document documentResume, String websiteResume) {
        user.setDocumentResume(documentResume);
        user.setWebsiteResume(websiteResume);

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
        updateMembership(user, userDTO);
        return saveUser(user);
    }

    public interface UserFinder {
        User getByEmail(String email);
    }

}
