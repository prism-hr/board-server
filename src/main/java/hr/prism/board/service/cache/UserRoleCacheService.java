package hr.prism.board.service.cache;

import hr.prism.board.domain.*;
import hr.prism.board.exception.ApiException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.repository.UserRoleRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class UserRoleCacheService {
    
    @Inject
    private UserRoleRepository userRoleRepository;
    
    @CacheEvict(key = "#user.id", value = "users")
    public void createUserRole(Resource resource, User user, Role role) {
        UserRole userRole = new UserRole().setResource(resource).setUser(user).setRole(role);
        userRoleRepository.save(userRole);
    }
    
    @CacheEvict(key = "#user.id", value = "users")
    public void deleteResourceUser(Resource resource, User user) {
        userRoleRepository.deleteByResourceAndUser(resource, user);
        checkSafety(resource, ExceptionCode.IRREMOVABLE_USER);
    }
    
    @CacheEvict(key = "#user.id", value = "users")
    public void deleteUserRole(Resource resource, User user, Role role) {
        List<UserRole> userRoles = userRoleRepository.findByResourceAndUser(resource, user);
        if (userRoles.size() <= 1) {
            throw new ApiException(ExceptionCode.IRREMOVABLE_USER_ROLE);
        }
        
        Long deletedCount = userRoleRepository.deleteByResourceAndUserAndRole(resource, user, role);
        if (deletedCount < 1) {
            throw new ApiException(ExceptionCode.NONEXISTENT_USER_ROLE);
        }
        
        checkSafety(resource, ExceptionCode.IRREMOVABLE_USER_ROLE);
    }
    
    private void checkSafety(Resource resource, ExceptionCode exceptionCode) {
        if (resource.getScope() == Scope.DEPARTMENT) {
            List<UserRole> remainingAdminRoles = userRoleRepository.findByResourceAndRole(resource, Role.ADMINISTRATOR);
            if (remainingAdminRoles.isEmpty()) {
                throw new ApiException(exceptionCode);
            }
        }
    }
    
}
