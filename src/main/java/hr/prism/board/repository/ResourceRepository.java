package hr.prism.board.repository;

import hr.prism.board.domain.Resource;
import hr.prism.board.domain.User;
import hr.prism.board.enums.Role;
import hr.prism.board.enums.Scope;
import hr.prism.board.enums.State;
import hr.prism.board.value.ChildResourceSummary;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@SuppressWarnings("JpaQlInspection")
public interface ResourceRepository extends MyRepository<Resource, Long> {

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
            "left join resource.categories category " +
            "left join userRole.categories userCategory " +
            "where resource.scope = :scope " +
            "and userRole.user = :user " +
            "and userRole.state in (:userRoleStates) " +
            "and (category.id is null or userRole.role in (:roles) or category.name = userCategory.name) " +
            "order by parent.name, resource.name")
    List<Resource> findByScopeAndUserAndRolesOrCategories(@Param("scope") Scope scope, @Param("user") User user, @Param("roles") List<Role> roles,
                                                          @Param("userRoleStates") List<State> userRoleStates);

    @Modifying
    @Query(value =
        "UPDATE resource " +
            "SET handle = CONCAT(:newHandle, SUBSTRING(handle, LENGTH(:handle) + 1)) " +
            "WHERE handle LIKE CONCAT(:handle, '/%')",
        nativeQuery = true)
    void updateHandle(@Param("handle") String handle, @Param("newHandle") String newHandle);

    @Query(value =
        "select new hr.prism.board.value.ChildResourceSummary(resource.scope, count(resource.id), max(resource.createdTimestamp)) " +
            "from Resource resource " +
            "where resource.parent = :parent " +
            "and resource.id <> resource.parent.id " +
            "and resource.state = :state")
    ChildResourceSummary findSummaryByParentAndState(@Param("parent") Resource parent, @Param("state") State state);

}
