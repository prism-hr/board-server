package hr.prism.board.repository;

import hr.prism.board.domain.*;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@SuppressWarnings("JpaQlInspection")
public interface ActivityDismissalRepository extends MyRepository<ActivityDismissal, Long> {

    ActivityDismissal findByActivityAndUser(Activity activity, User user);

    @Modifying
    @Query(value =
        "delete from ActivityDismissal activityDismissal " +
            "where activityDismissal.activity in (" +
            "select activity " +
            "from Activity activity " +
            "where activity.resource = :resource)")
    void deleteByResource(@Param("resource") Resource resource);

    @Modifying
    @Query(value =
        "delete from ActivityDismissal activityDismissal " +
            "where activityDismissal.activity in (" +
            "select activity " +
            "from Activity activity " +
            "where activity.userRole = :userRole)")
    void deleteByUserRole(@Param("userRole") UserRole userRole);

}
