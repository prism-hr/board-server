package hr.prism.board.repository;

import hr.prism.board.domain.Resource;
import hr.prism.board.domain.User;
import hr.prism.board.domain.UserRole;
import hr.prism.board.enums.Role;
import hr.prism.board.enums.State;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Transactional
public interface UserRoleRepository extends BoardEntityRepository<UserRole, Long> {

    UserRole findByUuid(String uuid);

    @Query(value =
        "select userRole " +
            "from ResourceRelation resourceRelation " +
            "inner join resourceRelation.resource1 parentResource " +
            "inner join parentResource.userRoles userRole " +
            "where resourceRelation.resource2 = :resource " +
            "and userRole.user = :user")
    List<UserRole> findByResourceAndUser(@Param("resource") Resource resource, @Param("user") User user);

    List<UserRole> findByResourceAndRole(Resource resource, Role role);

    UserRole findByResourceAndUserAndRole(Resource resource, User user, Role role);

    @Query(value =
        "select userRole " +
            "from UserRole userRole " +
            "where resource = :resource " +
            "and user.id = :userId " +
            "and role = :role")
    UserRole findByResourceAndUserIdAndRole(@Param("resource") Resource resource, @Param("userId") Long userId,
                                            @Param("role") Role role);

    @Query(value =
        "select userRole " +
            "from UserRole userRole " +
            "where userRole.resource = :resource " +
            "and userRole.user = :user " +
            "and userRole.role in (:roles)")
    List<UserRole> findByResourceAndUserAndRoles(@Param("resource") Resource resource, @Param("user") User user,
                                                 @Param("roles") List<Role> roles);

    Long deleteByResourceAndUser(Resource resource, User user);

    Long deleteByResourceAndUserAndRole(Resource resource, User user, Role role);

    @Query(value =
        "select userRole " +
            "from UserRole userRole " +
            "where userRole.user in (:users) " +
            "order by userRole.user")
    List<UserRole> findByUsersOrderByUser(@Param("users") List<User> users);

    @Modifying
    @Query(value =
        "update UserRole userRole " +
            "set userRole.user = :newUser " +
            "where userRole.user = :oldUser")
    void updateByUser(@Param("newUser") User newUser, @Param("oldUser") User oldUser);

    @Modifying
    @Query(value =
        "delete from UserRole userRole " +
            "where userRole.id in (:ids)")
    void deleteByIds(@Param("ids") List<Long> ids);

    @Query(value =
        "select userRole.id " +
            "from UserRole userRole " +
            "where userRole.user = :user " +
            "and userRole.role = :role " +
            "and userRole.state in (:userRoleStates) " +
            "and (userRole.expiryDate is null " +
            "or userRole.expiryDate >= :baseline)")
    List<Long> findIdsByUserAndRole(@Param("user") User user, @Param("role") Role role,
                                    @Param("userRoleStates") List<State> userRoleStates,
                                    @Param("baseline") LocalDate baseline);

}
