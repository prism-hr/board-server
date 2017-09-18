package hr.prism.board.repository;

import hr.prism.board.domain.Resource;
import hr.prism.board.domain.User;
import hr.prism.board.domain.UserRole;
import hr.prism.board.domain.UserRoleCategory;
import hr.prism.board.enums.Role;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@SuppressWarnings("JpaQlInspection")
public interface UserRoleCategoryRepository extends MyRepository<UserRoleCategory, Long> {

    @Modifying
    @Query(value =
        "delete from UserRoleCategory userRoleCategory " +
            "where userRoleCategory.userRole in (:userRoles)")
    void deleteByUserRoles(@Param("userRoles") List<UserRole> userRoles);

    @Modifying
    @Query(value =
        "delete from UserRoleCategory userRoleCategory " +
            "where userRoleCategory.userRole.id in ( " +
            "select id from UserRole userRole " +
            "where userRole.resource = :resource " +
            "and userRole.user = :user)")
    void deleteByResourceAndUser(@Param("resource") Resource resource, @Param("user") User user);

    @Modifying
    @Query(value =
        "delete from UserRoleCategory userRoleCategory " +
            "where userRoleCategory.userRole.id in ( " +
            "select id from UserRole userRole " +
            "where userRole.resource = :resource " +
            "and userRole.user = :user " +
            "and userRole.role = :role)")
    void deleteByResourceAndUserAndRole(@Param("resource") Resource resource, @Param("user") User user, @Param("role") Role role);

}
