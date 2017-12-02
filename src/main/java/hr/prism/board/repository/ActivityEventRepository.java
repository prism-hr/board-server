package hr.prism.board.repository;

import hr.prism.board.domain.*;
import hr.prism.board.enums.Role;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("JpaQlInspection")
public interface ActivityEventRepository extends MyRepository<ActivityEvent, Long> {
    
    ActivityEvent findByActivityAndUserAndEvent(Activity activity, User user, hr.prism.board.enums.ActivityEvent event);
    
    @Query(value =
        "select activityEvent " +
            "from ActivityEvent activityEvent " +
            "where activityEvent.activity in (:activities) " +
            "and activityEvent.user = :user " +
            "and activityEvent.event = :event")
    List<ActivityEvent> findByActivitiesAndUserAndEvent(@Param("activities") Collection<Activity> activities, @Param("user") User user,
        @Param("event") hr.prism.board.enums.ActivityEvent event);
    
    @Modifying
    @Query(value =
        "delete from ActivityEvent activityEvent " +
            "where activityEvent.activity in (" +
            "select activity " +
            "from Activity activity " +
            "left join activity.activityUsers activityUser " +
            "where activity.resource = :resource " +
            "and activity.userRole is null " +
            "and activity.resourceEvent is null " +
            "and activityUser.id is null)")
    void deleteByResource(@Param("resource") Resource resource);
    
    @Modifying
    @Query(value =
        "delete from ActivityEvent activityEvent " +
            "where activityEvent.activity in (" +
            "select activity " +
            "from Activity activity " +
            "where activity.resource = :resource " +
            "and activity.activity in (:activities))")
    void deleteByResourceAndActivities(@Param("resource") Resource resource, @Param("activities") List<hr.prism.board.enums.Activity> activities);
    
    @Modifying
    @Query(value =
        "delete from ActivityEvent activityEvent " +
            "where activityEvent.activity in (" +
            "select activity " +
            "from Activity activity " +
            "where activity.userRole = :userRole)")
    void deleteByUserRole(@Param("userRole") UserRole userRole);
    
    @Modifying
    @Query(value =
        "delete from ActivityEvent activityEvent " +
            "where activityEvent.activity in (" +
            "select activity " +
            "from Activity activity " +
            "where activity.userRole in (:userRoles))")
    void deleteByUserRoles(@Param("userRoles") List<UserRole> userRoles);
    
    @Modifying
    @Query(value =
        "delete from ActivityEvent activityEvent " +
            "where activityEvent.activity in ( " +
            "select activity " +
            "from Activity activity " +
            "inner join activity.userRole userRole " +
            "where userRole.resource = :resource " +
            "and userRole.user = :user)")
    void deleteByResourceAndUser(@Param("resource") Resource resource, @Param("user") User user);
    
    @Modifying
    @Query(value =
        "delete from ActivityEvent activityEvent " +
            "where activityEvent.activity in ( " +
            "select activity " +
            "from Activity activity " +
            "inner join activity.userRole userRole " +
            "where userRole.resource = :resource " +
            "and userRole.user = :user " +
            "and userRole.role = :role)")
    void deleteByResourceAndUserAndRole(@Param("resource") Resource resource, @Param("user") User user, @Param("role") Role role);
    
    @Modifying
    @Query(value =
        "INSERT INTO activity_event (activity_id, user_id, event, created_timestamp, updated_timestamp) " +
            "SELECT activity.id, :userId, :event, :baseline, :baseline " +
            "FROM activity " +
            "WHERE activity.resource_id = :resourceId " +
            "AND activity.activity IN (:activities)",
        nativeQuery = true)
    void insertByResourceIdActivitiesUserIdAndEvent(@Param("resourceId") Long resourceId,
        @Param("activities") List<String> activities, @Param("userId") Long userId, @Param("event") String event, @Param("baseline") LocalDateTime baseline);
    
}
