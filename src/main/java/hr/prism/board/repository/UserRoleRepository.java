package hr.prism.board.repository;

import hr.prism.board.domain.Resource;
import hr.prism.board.domain.Role;
import hr.prism.board.domain.User;
import hr.prism.board.domain.UserRole;

public interface UserRoleRepository extends MyRepository<UserRole, Long> {
    
    UserRole findByResourceUserAndRole(Resource resource, User user, Role role);
    
}
