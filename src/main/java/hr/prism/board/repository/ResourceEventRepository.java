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

    ResourceEvent findByResourceAndEventAndUser(Resource resource, hr.prism.board.enums.ResourceEvent event, User user);

    @Query(value =
        "select resourceEvent " +
            "from ResourceEvent resourceEvent " +
            "where resourceEvent.resource = :resource " +
            "and resourceEvent.event = :event " +
            "order by resourceEvent.id desc")
    List<ResourceEvent> findByResourceAndEventOrderByIdDesc(@Param("resource") Resource resource, @Param("event") hr.prism.board.enums.ResourceEvent event);

    @Query(value =
        "select new hr.prism.board.value.ResourceEventSummary(resourceEvent.event, count(distinct resourceEvent.user), max(resourceEvent.createdTimestamp)) " +
            "from ResourceEvent resourceEvent " +
            "where resourceEvent.resource = :resource " +
            "and resourceEvent.user is not null " +
            "group by resourceEvent.event")
    List<ResourceEventSummary> findUserSummaryByResource(@Param("resource") Resource resource);

    @Query(value =
        "select new hr.prism.board.value.ResourceEventSummary(resourceEvent.event, count(distinct resourceEvent.ipAddress), max(resourceEvent.createdTimestamp)) " +
            "from ResourceEvent resourceEvent " +
            "where resourceEvent.resource = :resource " +
            "and resourceEvent.ipAddress is not null " +
            "group by resourceEvent.event")
    List<ResourceEventSummary> findIpAddressSummaryByResource(@Param("resource") Resource resource);

}
