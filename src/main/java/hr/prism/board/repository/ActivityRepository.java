package hr.prism.board.repository;

import hr.prism.board.domain.*;
import hr.prism.board.enums.Role;
import hr.prism.board.enums.Scope;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Transactional
public interface ActivityRepository extends BoardEntityRepository<Activity, Long> {

    @Query(value =
        "select activityEvent.activity.id " +
            "from ActivityEvent activityEvent " +
            "where activityEvent.activity in (:activities) " +
            "and activityEvent.user.id = :userId " +
            "and activityEvent.event = :event")
    List<Long> findIdsByActivitiesAndUserIdAndEvent(@Param("activities") Collection<Activity> activities,
                                                    @Param("userId") Long userId,
                                                    @Param("event") hr.prism.board.enums.ActivityEvent event);

    @Query(value =
        "select activity " +
            "from Activity activity " +
            "where activity.resource = :resource " +
            "and activity.activity = :activity " +
            "and activity.userRole is null")
    Activity findByResourceAndActivity(@Param("resource") Resource resource,
                                       @Param("activity") hr.prism.board.enums.Activity activity);

    @Query(value =
        "select activity " +
            "from Activity activity " +
            "inner join activity.activityRoles activityRole " +
            "where activity.resource = :resource " +
            "and activity.activity = :activity " +
            "and activity.userRole is null " +
            "and activityRole.scope = :scope " +
            "and activityRole.role = :role")
    Activity findByResourceAndActivityAndRole(@Param("resource") Resource resource,
                                              @Param("activity") hr.prism.board.enums.Activity activity,
                                              @Param("scope") Scope scope, @Param("role") Role role);

    Activity findByUserRoleAndActivity(UserRole userRole, hr.prism.board.enums.Activity activity);

    Activity findByResourceEventAndActivity(ResourceEvent resourceEvent, hr.prism.board.enums.Activity activity);

    @Modifying
    @Query(value =
        "delete from Activity activity " +
            "where activity.resource = :resource " +
            "and activity.userRole is null " +
            "and activity.resourceEvent is null")
    void deleteByResource(@Param("resource") Resource resource);

    @Modifying
    @Query(value =
        "delete from Activity activity " +
            "where activity.resource = :resource " +
            "and activity.id not in (:ignores) " +
            "and activity.userRole is null " +
            "and activity.resourceEvent is null")
    void deleteByResourceWithIgnores(@Param("resource") Resource resource, @Param("ignores") List<Long> ignores);

    @Modifying
    @Query(value =
        "delete from Activity activity " +
            "where activity.resource = :resource " +
            "and activity.activity in (:activities)")
    void deleteByResourceAndActivities(@Param("resource") Resource resource,
                                       @Param("activities") List<hr.prism.board.enums.Activity> activities);

    @Modifying
    @Query(value =
        "delete from Activity activity " +
            "where activity.userRole = :userRole")
    void deleteByUserRole(@Param("userRole") UserRole userRole);

    @Modifying
    @Query(value =
        "delete from Activity activity " +
            "where activity.userRole in (:userRoles)")
    void deleteByUserRoles(@Param("userRoles") List<UserRole> userRoles);

    @Modifying
    @Query(value =
        "delete from Activity activity " +
            "where activity.userRole in (" +
            "select userRole " +
            "from UserRole userRole " +
            "where userRole.resource = :resource " +
            "and userRole.user = :user)")
    void deleteByResourceAndUser(@Param("resource") Resource resource, @Param("user") User user);

    @Modifying
    @Query(value =
        "delete from Activity activity " +
            "where activity.userRole in (" +
            "select userRole " +
            "from UserRole userRole " +
            "where userRole.resource = :resource " +
            "and userRole.user = :user " +
            "and userRole.role = :role)")
    void deleteByResourceAndUserAndRole(@Param("resource") Resource resource, @Param("user") User user,
                                        @Param("role") Role role);

    @Query(value =
        "select activity.id " +
            "from Activity activity " +
            "inner join activity.activityUsers activityUser " +
            "where activity.resource = :resource " +
            "and activity.userRole is null " +
            "and activity.resourceEvent is null")
    List<Long> findByResourceWithActivityUsers(@Param("resource") Resource resource);

}
