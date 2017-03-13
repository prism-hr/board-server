package hr.prism.board.domain;

import hr.prism.board.repository.UserRoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.ArrayUtils;

import javax.inject.Inject;

@Service
@Transactional
public class UserRoleService {
    
    @Inject
    private UserRoleRepository userRoleRepository;
    
    public void createUserRole(Resource resource, User user, Role role) {
        UserRole userRole = userRoleRepository.findByResourceUserAndRole(resource, user, role);
        if (userRole == null) {
            userRole = new UserRole().setResource(resource).setUser(user).setRole(role);
            userRoleRepository.save(userRole);
            
            resource.getUserRoles().add(userRole);
            user.getUserRoles().add(userRole);
        }
    }
    
    public boolean hasUserRole(Resource resource, User user, Role... roles) {
        if (ArrayUtils.isEmpty(roles)) {
            throw new IllegalStateException("No roles specified");
        }
        
        if (userRoleRepository.findByResourceUserAndRoles(resource, user, roles).size() > 0) {
            return true;
        }
        
        return false;
    }
    
}
