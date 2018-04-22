package hr.prism.board.repository;

import hr.prism.board.domain.Resource;
import hr.prism.board.domain.User;
import hr.prism.board.enums.CategoryType;
import hr.prism.board.enums.Role;
import hr.prism.board.enums.Scope;
import hr.prism.board.enums.State;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Transactional
public interface ResourceRepository extends JpaRepository<Resource, Long> {

    Resource findByHandle(String handle);

    @Query(value =
        "select resourceRelation.resource1 " +
            "from ResourceRelation resourceRelation " +
            "inner join resourceRelation.resource1 resource1 " +
            "where resourceRelation.resource2 = :resource " +
            "and resource1.scope = :scope")
    Resource findByResourceAndEnclosingScope(@Param("resource") Resource resource, @Param("scope") Scope scope);

    @Query(value =
        "select distinct resource " +
            "from Resource resource " +
            "inner join resource.parents parentRelation " +
            "inner join parentRelation.resource1 parent " +
            "inner join parent.userRoles userRole " +
            "inner join resource.parent parent " +
            "left join parent.categories resourceCategory " +
            "where resource.scope = :scope " +
            "and userRole.user = :user " +
            "and userRole.state in (:userRoleStates) " +
            "and (userRole.role in (:roles) " +
            "or resourceCategory.id is null and userRole.memberCategory is null " +
            "or resourceCategory.type = :categoryType and resourceCategory.name = userRole.memberCategory) " +
            "order by parent.name, resource.name")
    List<Resource> findByScopeAndUserAndRolesOrCategory(@Param("scope") Scope scope, @Param("user") User user,
                                                        @Param("roles") List<Role> roles,
                                                        @Param("categoryType") CategoryType categoryType,
                                                        @Param("userRoleStates") List<State> userRoleStates);

    @Modifying
    @Query(value =
        "UPDATE resource " +
            "SET handle = CONCAT(:newHandle, SUBSTRING(handle, LENGTH(:handle) + 1)) " +
            "WHERE handle LIKE CONCAT(:handle, '/%')",
        nativeQuery = true)
    void updateHandle(@Param("handle") String handle, @Param("newHandle") String newHandle);

    @Query(value =
        "select resource.id " +
            "from Resource resource " +
            "where resource.state in (:states) " +
            "and resource.updatedTimestamp < :baseline")
    List<Long> findByStatesAndLessThanUpdatedTimestamp(@Param("states") List<State> states,
                                                       @Param("baseline") LocalDateTime baseline);

    @Modifying
    @Query(value =
        "update Resource resource " +
            "set resource.updatedTimestamp = :baseline " +
            "where resource.id = :id")
    void updateUpdatedTimestampById(@Param("id") Long id, @Param("baseline") LocalDateTime baseline);

    @Modifying
    @Query(value =
        "update Resource resource " +
            "set resource.stateChangeTimestamp = :baseline " +
            "where resource.id = :id")
    void updateStateChangeTimestampById(@Param("id") Long id, @Param("baseline") LocalDateTime baseline);

    @Modifying
    @Query(value =
        "update Resource resource " +
            "set resource.previousState = resource.state, " +
            "resource.state = :state, " +
            "resource.stateChangeTimestamp = :baseline, " +
            "resource.updatedTimestamp = :baseline " +
            "where resource.id in (:resourceIds)")
    void updateStateByIds(@Param("resourceIds") Collection<Long> resourceIds, @Param("state") State state,
                          @Param("baseline") LocalDateTime baseline);

}
