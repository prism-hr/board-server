package hr.prism.board.repository;

import hr.prism.board.domain.Resource;
import hr.prism.board.domain.User;
import hr.prism.board.enums.CategoryType;
import hr.prism.board.enums.OauthProvider;
import hr.prism.board.enums.Role;
import hr.prism.board.enums.Scope;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

@SuppressWarnings("JpaQlInspection")
public interface UserRepository extends MyRepository<User, Long> {

    User findByEmail(String email);

    User findByOauthProviderAndOauthAccountId(OauthProvider provider, String oauthAccountId);

    @Query(value =
        "select user " +
            "from User user " +
            "where user.email = :email " +
            "and (user.password = :password " +
            "or (user.temporaryPassword = :password " +
            "and user.temporaryPasswordExpiryTimestamp >= :baseline))")
    User findByEmailAndPassword(@Param("email") String email, @Param("password") String password, @Param("baseline") LocalDateTime baseline);

    @Query(value =
        "select distinct userRole.user " +
            "from Resource resource " +
            "inner join resource.parents parent " +
            "inner join parent.resource1 enclosingResource " +
            "inner join enclosingResource.userRoles userRole " +
            "where parent.resource2 = :resource " +
            "and enclosingResource.scope = :enclosingScope " +
            "and userRole.role = :role")
    List<User> findByResourceAndEnclosingScopeAndRole(@Param("resource") Resource resource, @Param("enclosingScope") Scope enclosingScope, @Param("role") Role role);

    @Query(value =
        "select distinct userRole.user " +
            "from Resource resource " +
            "inner join resource.parents parent " +
            "inner join parent.resource2 resource " +
            "inner join resource.categories category " +
            "inner join parent.resource1 enclosingResource " +
            "inner join enclosingResource.userRoles userRole " +
            "inner join userRole.categories userCategory " +
            "where parent.resource2 = :resource " +
            "and enclosingResource.scope = :enclosingScope " +
            "and userRole.role = :role " +
            "and category.type = :categoryType " +
            "and category.name = userCategory.name")
    List<User> findByResourceAndEnclosingScopeAndRoleAndCategories(@Param("resource") Resource resource, @Param("enclosingScope") Scope enclosingScope,
                                                                   @Param("role") Role role, @Param("type") CategoryType categoryType);

    @Query(value =
        "select userRole.user " +
            "from UserRole userRole " +
            "where userRole.resource = :resource " +
            "and userRole.role = :role " +
            "and userRole.user not in (" +
            "select withoutUserRole.user " +
            "from UserRole withoutUserRole " +
            "where withoutUserRole.resource = :withoutResource " +
            "and withoutUserRole.role = :withoutRole)")
    List<User> findByRoleWithoutRole(
        @Param("resource") Resource resource, @Param("role") Role role, @Param("withoutResource") Resource withoutResource, @Param("withoutRole") Role withoutRole);

}
