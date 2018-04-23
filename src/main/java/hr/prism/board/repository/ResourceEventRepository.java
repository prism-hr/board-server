package hr.prism.board.repository;

import hr.prism.board.domain.Resource;
import hr.prism.board.domain.ResourceEvent;
import hr.prism.board.domain.User;
import hr.prism.board.value.ResourceEventSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface ResourceEventRepository extends JpaRepository<ResourceEvent, Long> {

    ResourceEvent findByReferral(String referral);

    @Query(value =
        "select resourceEvent " +
            "from ResourceEvent resourceEvent " +
            "where resourceEvent.id in (:ids)")
    List<ResourceEvent> findOnes(@Param("ids") List<Long> ids);

    @Query(value =
        "select max(resourceEvent.id) " +
            "from ResourceEvent resourceEvent " +
            "where resourceEvent.resource in (:resources) " +
            "and resourceEvent.event = :event " +
            "and resourceEvent.user = :user " +
            "group by resourceEvent.resource")
    <T extends Resource> List<Long> findMaxIdsByResourcesAndEventAndUser(
        @Param("resources") List<T> resources, @Param("event") hr.prism.board.enums.ResourceEvent event,
        @Param("user") User user);

    @Query(value =
        "select new hr.prism.board.value.ResourceEventSummary(resourceEvent.event, " +
            "count(distinct resourceEvent.user), max(resourceEvent.createdTimestamp)) " +
            "from ResourceEvent resourceEvent " +
            "where resourceEvent.resource = :resource " +
            "and resourceEvent.user is not null " +
            "and resourceEvent.referral is null " +
            "group by resourceEvent.event")
    List<ResourceEventSummary> findUserSummaryByResource(@Param("resource") Resource resource);

    @Query(value =
        "select new hr.prism.board.value.ResourceEventSummary(resourceEvent.event, " +
            "count(distinct resourceEvent.ipAddress), max(resourceEvent.createdTimestamp)) " +
            "from ResourceEvent resourceEvent " +
            "where resourceEvent.resource = :resource " +
            "and resourceEvent.ipAddress is not null " +
            "and resourceEvent.referral is null " +
            "group by resourceEvent.event")
    List<ResourceEventSummary> findIpAddressSummaryByResource(@Param("resource") Resource resource);

}
