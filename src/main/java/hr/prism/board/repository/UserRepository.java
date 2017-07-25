package hr.prism.board.repository;

import hr.prism.board.domain.Resource;
import hr.prism.board.domain.User;
import hr.prism.board.enums.*;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("JpaQlInspection")
public interface UserRepository extends MyRepository<User, Long> {

    String ACTIVE_CONSTRAINT =
        "(userRole.expiryDate is null or userRole.expiryDate >= :baseline)";

    String SUPPRESSION_CONSTRAINT =
        "userRole.user not in (" +
            "select userNotificationSuppression.user " +
            "from UserNotificationSuppression userNotificationSuppression " +
            "where userNotificationSuppression.resource <> :resource)";

    User findByUuid(String uuid);

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
        "select distinct userRole.user.id " +
            "from ResourceRelation relation " +
            "inner join relation.resource1 enclosingResource " +
            "inner join enclosingResource.userRoles userRole " +
            "where relation.resource2 = :resource " +
            "and userRole.user.id in (:userIds) " +
            "and userRole.state in (:userRoleStates) " +
            "and " + ACTIVE_CONSTRAINT)
    List<Long> findByResourceAndUserIds(@Param("resource") Resource resource,
                                        @Param("userIds") Collection<Long> userIds,
                                        @Param("userRoleStates") List<State> userRoleStates,
                                        @Param("baseline") LocalDate baseline);

    @Query(value =
        "select distinct userRole.user " +
            "from ResourceRelation relation " +
            "inner join relation.resource1 enclosingResource " +
            "inner join enclosingResource.userRoles userRole " +
            "where relation.resource2 = :resource " +
            "and enclosingResource.scope = :enclosingScope " +
            "and userRole.role = :role " +
            "and userRole.state in (:userRoleStates) " +
            "and " + ACTIVE_CONSTRAINT + " " +
            "and " + SUPPRESSION_CONSTRAINT)
    List<User> findByResourceAndEnclosingScopeAndRole(@Param("resource") Resource resource, @Param("enclosingScope") Scope enclosingScope, @Param("role") Role role,
                                                      @Param("userRoleStates") List<State> userRoleStates, @Param("baseline") LocalDate baseline);

    @Query(value =
        "select distinct userRole.user " +
            "from ResourceRelation relation " +
            "inner join relation.resource1 enclosingResource " +
            "inner join enclosingResource.userRoles userRole " +
            "inner join userRole.categories userRoleCategory " +
            "inner join relation.resource2 resource " +
            "inner join resource.categories resourceCategory " +
            "where relation.resource2 = :resource " +
            "and enclosingResource.scope = :enclosingScope " +
            "and userRole.role = :role " +
            "and userRole.state in (:userRoleStates) " +
            "and resourceCategory.type = :categoryType " +
            "and resourceCategory.name = userRoleCategory.name " +
            "and " + ACTIVE_CONSTRAINT + " " +
            "and " + SUPPRESSION_CONSTRAINT)
    List<User> findByResourceAndEnclosingScopeAndRoleAndCategories(@Param("resource") Resource resource, @Param("enclosingScope") Scope enclosingScope,
                                                                   @Param("role") Role role, @Param("userRoleStates") List<State> userRoleStates,
                                                                   @Param("categoryType") CategoryType categoryType, @Param("baseline") LocalDate baseline);

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
