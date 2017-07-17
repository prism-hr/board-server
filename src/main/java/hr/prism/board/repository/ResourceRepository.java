package hr.prism.board.repository;

import hr.prism.board.domain.Resource;
import hr.prism.board.domain.User;
import hr.prism.board.enums.Scope;
import hr.prism.board.enums.State;
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
        "where resource.scope = :scope " +
        "and userRole.user = :user " +
        "and userRole.state in (:userRoleStates) " +
        "and category.id is null " +
        "order by parent.name, resource.name")
    List<Resource> findByUserAndScope(@Param("user") User user, @Param("scope") Scope scope, @Param("userRoleStates") State[] userRoleStates);

    @Query(value =
        "select distinct resource " +
            "from Resource resource " +
            "inner join resource.parents parentRelation " +
            "inner join parentRelation.resource1 parent " +
            "inner join parent.userRoles userRole " +
            "inner join resource.parent parent " +
            "inner join resource.categories category " +
            "inner join userRole.categories userCategory " +
            "where resource.scope = :scope " +
            "and userRole.user = :user " +
            "and userRole.state in (:userRoleStates) " +
            "and category.name = userCategory.name " +
            "order by parent.name, resource.name")
    List<Resource> findByUserAndScopeAndCategories(@Param("user") User user, @Param("scope") Scope scope, @Param("userRoleStates") State[] userRoleStates);

    @Modifying
    @Query(value =
        "update resource " +
            "set handle = concat(:newHandle, substring(handle, length(:handle) + 1)) " +
            "where handle like concat(:handle, '/%')",
        nativeQuery = true)
    void updateHandle(@Param("handle") String handle, @Param("newHandle") String newHandle);

}
