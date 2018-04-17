package hr.prism.board.service;

import hr.prism.board.domain.User;
import hr.prism.board.dto.UserDTO;
import hr.prism.board.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.UUID;

import static hr.prism.board.utils.BoardUtils.makeSoundex;

@Service
@Transactional
public class NewUserService {

    private static final String TEST_USER_SUFFIX = "@test.prism.hr";

    private final UserRepository userRepository;

    @Inject
    public NewUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User getOrCreateUser(UserDTO userDTO, UserFinder userFinder) {
        User user = null;
        Long userId = userDTO.getId();
        if (userId != null) {
            user = userRepository.findOne(userId);
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
            user.setIndexData(makeSoundex(user.getGivenName(), user.getSurname()));
            user.setTestUser(user.getEmail().endsWith(TEST_USER_SUFFIX));
            user = userRepository.save(user);

            user.setCreatorId(user.getId());
            return user;
        }

        return user;
    }


    public interface UserFinder {
        User getByEmail(String email);
    }

}
