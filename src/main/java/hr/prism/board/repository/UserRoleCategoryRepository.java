package hr.prism.board.repository;

import hr.prism.board.domain.Resource;
import hr.prism.board.domain.User;
import hr.prism.board.domain.UserRole;
import hr.prism.board.domain.UserRoleCategory;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRoleCategoryRepository extends MyRepository<UserRoleCategory, Long> {

    Long deleteByUserRole(@Param("userRole") UserRole userRole);

    @Modifying
    @Query(value =
        "delete from UserRoleCategory userRoleCategory " +
            "where userRoleCategory.userRole.id in ( " +
            "select id from UserRole userRole " +
            "where userRole.resource = :resource " +
            "and userRole.user = :user)")
    void deleteByResourceAndUser(@Param("resource") Resource resource, @Param("user") User user);
}
