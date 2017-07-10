package hr.prism.board.repository;

import hr.prism.board.domain.Resource;
import hr.prism.board.domain.User;
import hr.prism.board.domain.UserNotificationSuppression;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@SuppressWarnings("JpaQlInspection")
public interface UserNotificationSuppressionRepository extends MyRepository<UserNotificationSuppression, Long> {

    List<UserNotificationSuppression> findByUser(User user);

    UserNotificationSuppression findByUserAndResource(User user, Resource resource);

    @Modifying
    @Query(value =
        "delete from UserNotificationSuppression userNotificationSuppression " +
            "where userNotificationSuppression.user = :user")
    void deleteByUser(@Param("user") User user);

    @Modifying
    @Query(value =
        "delete from UserNotificationSuppression userNotificationSuppression " +
            "where userNotificationSuppression.user = :user " +
            "and userNotificationSuppression.resource = :resource")
    void deleteByUserAndResource(@Param("user") User user, @Param("resource") Resource resource);

    @Modifying
    @Query(value =
        "INSERT INTO user_notification_suppression(user_id, resource_id, created_timestamp) " +
            "SELECT user_role.user_id, user_role.resource_id " +
            "FROM user_role " +
            "LEFT JOIN user_notification_suppression " +
            "ON user_role.user_id = user_notification_suppression.user_id " +
            "AND user_role.resource_id = user_notification_suppression.resource_id " +
            "WHERE user_notificiation_suppression_id IS NULL",
        nativeQuery = true)
    void insertByUserId(@Param("userId") Long userId);

}
