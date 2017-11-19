package hr.prism.board.service.cache;

import hr.prism.board.domain.Resource;
import hr.prism.board.domain.User;
import hr.prism.board.enums.OauthProvider;
import hr.prism.board.enums.Role;
import hr.prism.board.enums.Scope;
import hr.prism.board.exception.BoardForbiddenException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.repository.UserRepository;
import hr.prism.board.service.ResourceService;
import hr.prism.board.utils.BoardUtils;
import hr.prism.board.value.ResourceSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserCacheService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserCacheService.class);

    @Inject
    private CacheManager cacheManager;

    @Inject
    private UserRepository userRepository;

    @Inject
    private ResourceService resourceService;

    @Cacheable(key = "#userId", value = "users")
    public User findOne(Long userId) {
        User user = userRepository.findOne(userId);
        appendScopes(user);
        return user;
    }

    @CacheEvict(key = "#userId", value = "users", beforeInvocation = true)
    public User findOneFresh(Long userId) {
        User user = userRepository.findOne(userId);
        appendScopes(user);
        return user;
    }

    @Cacheable(key = "#user.id", value = "users")
    public User updateUser(User user) {
        setIndexData(user);
        appendScopes(user);
        return userRepository.update(user);
    }

    @Cacheable(key = "#user.id", value = "users")
    public void deleteUser(User user) {
        try {
            userRepository.delete(user);
        } catch (Exception e) {
            // Should not happen but in case it does, not a reason to crash the process
            LOGGER.warn("Could not delete user: " + user.toString(), e);
        }
    }

    public User saveUser(User user) {
        user = userRepository.save(user);
        setIndexData(user);
        appendScopes(user);
        cacheManager.getCache("users").put(user.getId(), user);
        return user;
    }

    public User findByEmail(String email) {
        User user = userRepository.findByEmail(email);
        appendScopes(user);
        return user;
    }

    public User findByEmail(Resource resource, String email, Role role) {
        List<User> users = userRepository.findByEmail(resource, email, role);
        if (users.isEmpty()) {
            return null;
        }

        User user = users.stream().filter(usr -> usr.getEmail().equals(email)).findFirst().orElse(users.get(0));
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
            throw new BoardForbiddenException(ExceptionCode.UNKNOWN_USER, "User with user role uuid; " + uuid + " cannot be found");
        }

        return user;
    }

    public User findByOauthProviderAndOauthAccountId(OauthProvider provider, String oauthAccountId) {
        User user = userRepository.findByOauthProviderAndOauthAccountId(provider, oauthAccountId);
        appendScopes(user);
        return user;
    }

    public void setIndexData(User user) {
        user.setIndexData(BoardUtils.makeSoundex(user.getGivenName(), user.getSurname()));
    }

    private void appendScopes(User user) {
        if (user != null) {
            List<Scope> scopes = resourceService.findSummaryByUserAndRole(user, Role.ADMINISTRATOR).stream().map(ResourceSummary::getKey).collect(Collectors.toList());
            if (scopes.contains(Scope.DEPARTMENT)) {
                user.setScopes(Arrays.asList(Scope.DEPARTMENT, Scope.BOARD, Scope.POST));
            } else if (scopes.contains(Scope.BOARD)) {
                user.setScopes(Arrays.asList(Scope.BOARD, Scope.POST));
            } else {
                user.setScopes(Collections.singletonList(Scope.POST));
            }
        }
    }

}
