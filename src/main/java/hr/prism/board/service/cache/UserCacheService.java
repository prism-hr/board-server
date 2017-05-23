package hr.prism.board.service.cache;

import hr.prism.board.domain.User;
import hr.prism.board.repository.UserRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;

@Service
@Transactional
public class UserCacheService {
    
    @Inject
    private UserRepository userRepository;
    
    @Cacheable(key = "#userId", value = "users")
    public User findOne(Long userId) {
        return userRepository.findOne(userId);
    }
    
}
