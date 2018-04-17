package hr.prism.board.service;

import com.google.common.collect.ImmutableList;
import hr.prism.board.domain.Resource;
import hr.prism.board.domain.User;
import hr.prism.board.enums.OauthProvider;
import hr.prism.board.enums.Role;
import hr.prism.board.enums.Scope;
import hr.prism.board.exception.BoardForbiddenException;
import hr.prism.board.repository.UserRepository;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;

import static hr.prism.board.enums.Scope.DEPARTMENT;
import static hr.prism.board.enums.Scope.POST;
import static hr.prism.board.exception.ExceptionCode.UNKNOWN_USER;
import static hr.prism.board.utils.BoardUtils.makeSoundex;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

@Service
@Transactional
public class UserCacheService {

    private static final String TEST_USER_SUFFIX = "@test.prism.hr";

    private final UserRepository userRepository;

    private final EntityManager entityManager;

    @Inject
    public UserCacheService(UserRepository userRepository, EntityManager entityManager) {
        this.userRepository = userRepository;
        this.entityManager = entityManager;
    }

    public User getUser(Long userId) {
        User user = userRepository.findOneExtended(userId);
        appendScopes(user);
        return user;
    }

    public User getUserFromDatabase(Long userId) {
        User user = userRepository.findOneExtended(userId);
        appendScopes(user);
        return user;
    }

    @Cacheable(key = "#user.id", value = "users")
    public User updateUser(User user) {
        setIndexData(user);
        appendScopes(user);
        return userRepository.update(user);
    }

    @CacheEvict(key = "#user.id", value = "users")
    public void deleteUser(User user) {
        userRepository.delete(user);
    }

    public User saveUser(User user) {
        setIndexData(user);
        user = userRepository.save(user);
        setCreator(user);
        entityManager.flush();

        Long userId = user.getId();
        user = userRepository.findOneExtended(userId);

        appendScopes(user);
        return user;
    }

    public void setCreator(User user) {
        user.setTestUser(user.getEmail().endsWith(TEST_USER_SUFFIX));
        user.setCreatorId(user.getId());
    }

    public User findByEmail(String email) {
        User user = userRepository.findByEmail(email);
        appendScopes(user);
        return user;
    }

    public User findByEmail(Resource resource, String email, Role role) {
        List<User> potentialUsers = userRepository.findByEmail(resource, email, role);
        if (potentialUsers.isEmpty()) {
            return null;
        }

        User user = potentialUsers.stream()
            .filter(potentialUser -> potentialUser.getEmail().equals(email))
            .findFirst()
            .orElse(potentialUsers.get(0));

        appendScopes(user);
        return user;
    }

    public User findByUserRoleUuid(String uuid) {
        User user = userRepository.findByUserRoleUuid(uuid);
        appendScopes(user);
        return user;
    }

    public User findByUserRoleUuidSecured(String uuid) {
        User user = userRepository.findByUserRoleUuid(uuid);
        if (user == null) {
            throw new BoardForbiddenException(UNKNOWN_USER, "User with user role uuid; " + uuid + " cannot be found");
        }

        return user;
    }

    public User findByOauthProviderAndOauthAccountId(OauthProvider provider, String oauthAccountId) {
        User user = userRepository.findByOauthProviderAndOauthAccountId(provider, oauthAccountId);
        appendScopes(user);
        return user;
    }

    public void setIndexData(User user) {
        user.setIndexData(makeSoundex(user.getGivenName(), user.getSurname()));
    }

    private void appendScopes(User user) {
        if (user == null) {
            return;
        }

        List<Pair<Scope, Role>> permissions =
            user.getUserRoles()
                .stream()
                .map(userRole -> Pair.of(userRole.getResource().getScope(), userRole.getRole()))
                .distinct()
                .collect(toList());

        if (permissions.contains(Pair.of(DEPARTMENT, Role.ADMINISTRATOR))) {
            user.setPermissions(ImmutableList.of(DEPARTMENT, POST));
        } else {
            user.setPermissions(singletonList(POST));
        }
    }

}
