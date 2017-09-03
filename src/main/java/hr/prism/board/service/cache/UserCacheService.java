package hr.prism.board.service.cache;

import hr.prism.board.domain.User;
import hr.prism.board.repository.UserRepository;
import hr.prism.board.util.BoardUtils;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;

@Service
@Transactional
public class UserCacheService {

    @Inject
    private CacheManager cacheManager;

    @Inject
    private UserRepository userRepository;

    @Cacheable(key = "#userId", value = "users")
    public User findOne(Long userId) {
        return userRepository.findOne(userId);
    }

    @CacheEvict(key = "#userId", value = "users", beforeInvocation = true)
    public User findOneFresh(Long userId) {
        return userRepository.findOne(userId);
    }

    public User saveUser(User user) {
        user = userRepository.save(user);
        setIndexData(user);
        cacheManager.getCache("users").put(user.getId(), user);
        return user;
    }

    @Cacheable(key = "#user.id", value = "users")
    public User updateUser(User user) {
        setIndexData(user);
        return userRepository.update(user);
    }

    private void setIndexData(User user) {
        user.setIndexData(BoardUtils.makeSoundexRemovingStopWords(user.getGivenName(), user.getSurname(), user.getEmail()));
    }

}
