package hr.prism.board.repository;

import hr.prism.board.domain.Resource;
import hr.prism.board.domain.User;
import hr.prism.board.domain.UserNotificationSuppression;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Transactional
public interface UserNotificationSuppressionRepository extends JpaRepository<UserNotificationSuppression, Long> {

    @Query(value =
        "select userNotificationSuppression.resource " +
            "from UserNotificationSuppression userNotificationSuppression " +
            "where userNotificationSuppression.user = :user")
    @SuppressWarnings("SpringDataRepositoryMethodReturnTypeInspection")
    List<Resource> findByUser(@Param("user") User user);

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
            "and userNotificationSuppression.resource.id = :resourceId")
    void deleteByUserAndResourceId(@Param("user") User user, @Param("resourceId") Long resourceId);

    @Modifying
    @Query(value =
        "INSERT INTO user_notification_suppression (user_id, resource_id, creator_id, created_timestamp, " +
            "updated_timestamp) " +
            "SELECT user_role.user_id, suppressed.id, :userId, :baseline, :baseline " +
            "FROM user_role " +
            "INNER JOIN resource_relation " +
            "ON user_role.resource_id = resource_relation.resource1_id " +
            "INNER JOIN resource as suppressed " +
            "ON resource_relation.resource2_id = suppressed.id " +
            "LEFT JOIN user_notification_suppression " +
            "ON user_role.user_id = user_notification_suppression.user_id " +
            "AND suppressed.id = user_notification_suppression.resource_id " +
            "WHERE user_role.user_id = :userId " +
            "AND user_role.state IN (:userRoleStates) " +
            "AND suppressed.scope = :scope " +
            "AND user_notification_suppression.id IS NULL " +
            "GROUP BY user_role.user_id, suppressed.id",
        nativeQuery = true)
    @SuppressWarnings("SqlResolve")
    void insertByUserId(@Param("userId") Long userId, @Param("baseline") LocalDateTime baseline,
                        @Param("scope") String scope, @Param("userRoleStates") List<String> userRoleStates);

}
