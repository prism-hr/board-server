package hr.prism.board.repository;

import hr.prism.board.domain.Resource;
import hr.prism.board.domain.ResourceEvent;
import hr.prism.board.domain.User;
import hr.prism.board.value.ResourceEventSummary;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

@SuppressWarnings("JpaQlInspection")
public interface ResourceEventRepository extends MyRepository<ResourceEvent, Long> {

    ResourceEvent findFirstByResourceAndEventAndUserOrderByIdDesc(Resource resource, hr.prism.board.enums.ResourceEvent event, User user);

    ResourceEvent findFirstByResourceAndEventAndIpAddressOrderByIdDesc(Resource resource, hr.prism.board.enums.ResourceEvent event, String ipAddress);

    @Query(value =
        "select resourceEvent " +
            "from ResourceEvent resourceEvent " +
            "where resourceEvent.resource = :resource " +
            "order by resourceEvent.id desc")
    List<ResourceEvent> findByResourceOrderByIdDesc(@Param("resource") Resource resource);

    @Query(value =
        "select resourceEvent " +
            "from ResourceEvent resourceEvent " +
            "where resourceEvent.resource = :resource " +
            "and resourceEvent.event = :event " +
            "order by resourceEvent.id desc")
    List<ResourceEvent> findByResourceAndEventOrderByIdDesc(@Param("resource") Resource resource, @Param("event") hr.prism.board.enums.ResourceEvent event);

    @Query(value =
        "select new hr.prism.board.value.ResourceEventSummary(resourceEvent.event, count(resourceEvent.id), max(resourceEvent.createdTimestamp)) " +
            "from ResourceEvent resourceEvent " +
            "where resourceEvent.resource = :resource " +
            "group by resourceEvent.event")
    List<ResourceEventSummary> findSummaryByResource(@Param("resource") Resource resource);

    @Query(value =
        "select resourceEvent.resource.id " +
            "from ResourceEvent resourceEvent " +
            "where resourceEvent.resource.id in (:postIds) " +
            "and resourceEvent.user = :user " +
            "and (resourceEvent.documentResume is not null or resourceEvent.websiteResume is not null) " +
            "and resourceEvent.coveringNote is not null")
    List<Long> findResourceIdsRespondedTo(@Param("postIds") Collection<Long> postIds, @Param("user") User user);

}
