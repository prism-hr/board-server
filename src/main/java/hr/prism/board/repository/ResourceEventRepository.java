package hr.prism.board.repository;

import hr.prism.board.domain.Resource;
import hr.prism.board.domain.ResourceEvent;
import hr.prism.board.domain.User;
import hr.prism.board.value.ResourceEventSummary;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@SuppressWarnings("JpaQlInspection")
public interface ResourceEventRepository extends MyRepository<ResourceEvent, Long> {

    ResourceEvent findByReferral(String referral);

    @Query(value =
        "select resourceEvent " +
            "from ResourceEvent resourceEvent " +
            "where resourceEvent.resource = :resource " +
            "and resourceEvent.event = :event " +
            "and resourceEvent.user = :user " +
            "group by resourceEvent.resource " +
            "order by resourceEvent.id desc")
    ResourceEvent findByResourceAndEventAndUser(@Param("resource") Resource resource, @Param("event") hr.prism.board.enums.ResourceEvent event, @Param("user") User user);

    @Query(value =
        "select resourceEvent " +
            "from ResourceEvent resourceEvent " +
            "where resourceEvent.resource = :resource " +
            "and resourceEvent.event = :event " +
            "and resourceEvent.referral is null " +
            "group by resourceEvent.resource, resourceEvent.user " +
            "order by resourceEvent.id desc")
    List<ResourceEvent> findByResourceAndEvent(@Param("resource") Resource resource, @Param("event") hr.prism.board.enums.ResourceEvent event);

    @Query(value =
        "select new hr.prism.board.value.ResourceEventSummary(resourceEvent.event, count(distinct resourceEvent.user), max(resourceEvent.createdTimestamp)) " +
            "from ResourceEvent resourceEvent " +
            "where resourceEvent.resource = :resource " +
            "and resourceEvent.user is not null " +
            "and resourceEvent.referral is null " +
            "group by resourceEvent.event")
    List<ResourceEventSummary> findUserSummaryByResource(@Param("resource") Resource resource);

    @Query(value =
        "select new hr.prism.board.value.ResourceEventSummary(resourceEvent.event, count(distinct resourceEvent.ipAddress), max(resourceEvent.createdTimestamp)) " +
            "from ResourceEvent resourceEvent " +
            "where resourceEvent.resource = :resource " +
            "and resourceEvent.user is null " +
            "and resourceEvent.ipAddress is not null " +
            "and resourceEvent.referral is null " +
            "group by resourceEvent.event")
    List<ResourceEventSummary> findIpAddressSummaryByResource(@Param("resource") Resource resource);

    @Query(value =
        "select resourceEvent " +
            "from ResourceEvent resourceEvent " +
            "where resourceEvent.resource.id in (:resourceIds) " +
            "and resourceEvent.event = :event " +
            "and resourceEvent.user = :user " +
            "group by resourceEvent.resource " +
            "order by resourceEvent.id desc")
    List<ResourceEvent> findByResourceIdsAndEventAndUser(@Param("resourceIds") List<Long> resourceIds, @Param("event") hr.prism.board.enums.ResourceEvent event,
                                                         @Param("user") User user);

}
