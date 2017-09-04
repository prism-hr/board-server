package hr.prism.board.repository;

import hr.prism.board.domain.*;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
            "where activity.resource = :resource " +
            "and activity.userRole is null)")
    void deleteByResource(@Param("resource") Resource resource);

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
            "where activityEvent.activity in ( " +
            "select activity " +
            "from Activity activity " +
            "inner join activity.userRole userRole " +
            "where userRole.resource = :resource " +
            "and userRole.user = :user)")
    void deleteByResourceAndUser(@Param("resource") Resource resource, @Param("user") User user);

}
