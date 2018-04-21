package hr.prism.board.repository;

import hr.prism.board.domain.Resource;
import hr.prism.board.domain.User;
import hr.prism.board.enums.*;
import hr.prism.board.value.UserNotification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.data.jpa.repository.EntityGraph.EntityGraphType.FETCH;

@Transactional
public interface UserRepository extends BoardEntityRepository<User, Long> {

    @Query(value =
        "select distinct user " +
            "from User user " +
            "where user.id = :id")
    @EntityGraph(value = "user.extended", type = FETCH)
    User findOne(@Param("id") Long id);

    User findByUuid(String uuid);

    User findByEmail(String email);

    @Query(value =
        "select user " +
            "from User user " +
            "where user.email = :email " +
            "or user.id in (" +
            "select userRole.user.id " +
            "from UserRole userRole " +
            "where userRole.resource = :resource " +
            "and userRole.email = :email " +
            "and userRole.role = :role)")
    List<User> findByEmail(@Param("resource") Resource resource, @Param("email") String email,
                           @Param("role") Role role);

    @Query(value =
        "select userRole.user " +
            "from UserRole userRole " +
            "where userRole.uuid = :uuid")
    User findByUserRoleUuid(@Param("uuid") String uuid);

    @Query(value =
        "select user " +
            "from User user " +
            "where user.email = :email " +
            "and user.id <> :id")
    User findByEmailAndNotId(@Param("email") String email, @Param("id") Long id);

    User findByOauthProviderAndOauthAccountId(OauthProvider provider, String oauthAccountId);

    User findByPasswordResetUuid(String passwordResetUuid);

    @Query(value =
        "select distinct userRole.user.id " +
            "from ResourceRelation relation " +
            "inner join relation.resource1 enclosingResource " +
            "inner join enclosingResource.userRoles userRole " +
            "where relation.resource2 = :resource " +
            "and userRole.user.id in (:userIds) " +
            "and userRole.state in (:userRoleStates)")
    List<Long> findByResourceAndUserIds(@Param("resource") Resource resource, @Param("userIds") List<Long> userIds,
                                        @Param("userRoleStates") List<State> userRoleStates);

    @Query(value =
        "select distinct new hr.prism.board.value.UserNotification(userRole.user, userRole.uuid) " +
            "from ResourceRelation relation " +
            "inner join relation.resource1 enclosingResource " +
            "inner join enclosingResource.userRoles userRole " +
            "where relation.resource2 = :resource " +
            "and enclosingResource.scope = :enclosingScope " +
            "and userRole.role = :role " +
            "and userRole.state in (:userRoleStates) " +
            "and (userRole.expiryDate is null " +
            "or userRole.expiryDate >= :baseline) " +
            "and userRole.user not in (" +
            "select userNotificationSuppression.user " +
            "from UserNotificationSuppression userNotificationSuppression " +
            "where userNotificationSuppression.resource <> :resource)")
    List<UserNotification> findByResourceAndEnclosingScopeAndRole(@Param("resource") Resource resource,
                                                                  @Param("enclosingScope") Scope enclosingScope,
                                                                  @Param("role") Role role,
                                                                  @Param("userRoleStates") List<State> userRoleStates,
                                                                  @Param("baseline") LocalDate baseline);

    @Query(value =
        "select distinct new hr.prism.board.value.UserNotification(userRole.user, userRole.uuid) " +
            "from ResourceRelation relation " +
            "inner join relation.resource1 enclosingResource " +
            "inner join relation.resource2 resource " +
            "inner join enclosingResource.userRoles userRole " +
            "left join resource.categories resourceCategory " +
            "where relation.resource2 = :resource " +
            "and enclosingResource.scope = :enclosingScope " +
            "and userRole.role = :role " +
            "and userRole.state in (:userRoleStates) " +
            "and (resourceCategory.id is null " +
            "or resourceCategory.type = :categoryType " +
            "and resourceCategory.name = userRole.memberCategory) " +
            "and (userRole.expiryDate is null " +
            "or userRole.expiryDate >= :baseline) " +
            "and userRole.user not in (" +
            "select userNotificationSuppression.user " +
            "from UserNotificationSuppression userNotificationSuppression " +
            "where userNotificationSuppression.resource <> :resource) " +
            "and relation.resource2 not in (" +
            "select resourceEvent.resource " +
            "from ResourceEvent resourceEvent " +
            "where resourceEvent.resource = :resource " +
            "and resourceEvent.user = userRole.user)")
    List<UserNotification> findByResourceAndEnclosingScopeAndRoleAndCategory(
        @Param("resource") Resource resource, @Param("enclosingScope") Scope enclosingScope, @Param("role") Role role,
        @Param("userRoleStates") List<State> userRoleStates, @Param("categoryType") CategoryType categoryType,
        @Param("baseline") LocalDate baseline);

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
        @Param("resource") Resource resource, @Param("role") Role role,
        @Param("withoutResource") Resource withoutResource, @Param("withoutRole") Role withoutRole);

    @Query(value =
        "select userRole.user.id " +
            "from UserRole userRole " +
            "where userRole.resource = :resource " +
            "and userRole.role in (:roles) " +
            "and userRole.state = :state " +
            "order by userRole.id desc")
    List<Long> findByResourceAndRolesAndState(@Param("resource") Resource resource, @Param("roles") List<Role> roles,
                                              @Param("state") State state);

    @Query(value =
        "select resourceEvent.user.id " +
            "from ResourceEvent resourceEvent " +
            "where resourceEvent.resource = :resource " +
            "and resourceEvent.event in (:events) " +
            "and resourceEvent.referral is null " +
            "group by resourceEvent.resource, resourceEvent.user " +
            "order by resourceEvent.id desc")
    List<Long> findByResourceAndEvents(@Param("resource") Resource resource,
                                       @Param("events") List<ResourceEvent> events);

    @Query(value =
        "select user.id " +
            "from User user " +
            "where user.testUser = :testUser")
    List<Long> findByTestUser(@Param("testUser") Boolean testUser);

}
