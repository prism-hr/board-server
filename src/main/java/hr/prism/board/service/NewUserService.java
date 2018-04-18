package hr.prism.board.service;

import hr.prism.board.authentication.AuthenticationToken;
import hr.prism.board.domain.Resource;
import hr.prism.board.domain.User;
import hr.prism.board.dto.LocationDTO;
import hr.prism.board.dto.UserDTO;
import hr.prism.board.enums.AgeRange;
import hr.prism.board.enums.Gender;
import hr.prism.board.enums.Role;
import hr.prism.board.exception.BoardForbiddenException;
import hr.prism.board.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;
import java.util.UUID;

import static hr.prism.board.exception.ExceptionCode.UNAUTHENTICATED_USER;
import static hr.prism.board.utils.BoardUtils.makeSoundex;

@Service
@Transactional
public class NewUserService {

    private static final String TEST_USER_SUFFIX = "@test.prism.hr";

    private final UserRepository userRepository;

    private final LocationService locationService;

    @Inject
    public NewUserService(UserRepository userRepository, LocationService locationService) {
        this.userRepository = userRepository;
        this.locationService = locationService;
    }

    public User getById(Long id) {
        return userRepository.findOne(id);
    }

    public User getByEmail(String email) {
        return userRepository.findByEmail(email);
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

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null ? null : ((AuthenticationToken) authentication).getUser();
    }

    public User getCurrentUserSecured() {
        User user = getCurrentUser();
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

    private User createUser(UserDTO userDTO) {
        User user = new User();
        user.setUuid(UUID.randomUUID().toString());

        user.setGivenName(userDTO.getGivenName());
        user.setSurname(userDTO.getSurname());

        user.setEmail(userDTO.getEmail());
        user.setIndexData(makeSoundex(user.getGivenName(), user.getSurname()));

        user.setTestUser(user.getEmail().endsWith(TEST_USER_SUFFIX));
        updateMembership(user, userDTO);
        user = userRepository.save(user);

        user.setCreatorId(user.getId());
        return user;
    }

    public interface UserFinder {
        User getByEmail(String email);
    }

}
