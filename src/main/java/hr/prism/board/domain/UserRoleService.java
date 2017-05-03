package hr.prism.board.domain;

import hr.prism.board.repository.UserRoleRepository;
import hr.prism.board.service.ResourceService;
import hr.prism.board.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.ArrayUtils;

import javax.inject.Inject;

@Service
@Transactional
public class UserRoleService {
    
    @Inject
    private UserRoleRepository userRoleRepository;
    
    @Inject
    private ResourceService resourceService;
    
    @Inject
    private UserService userService;
    
    public void createUserRole(Long resourceId, Long userId, Role role) {
        Resource resource = resourceService.findOne(resourceId);
        User user = userService.findOne(userId);
        createUserRole(resource, user, role);
    }
    
    public void createUserRole(Resource resource, User user, Role role) {
        if (role == Role.PUBLIC) {
            throw new IllegalStateException("Public role is anonymous - cannot be assigned to a user");
        }
        
        UserRole userRole = userRoleRepository.findByResourceAndUserAndRole(resource, user, role);
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
        
        return userRoleRepository.findByResourceAndUserAndRoles(resource, user, roles).size() > 0;
    }
    
}
