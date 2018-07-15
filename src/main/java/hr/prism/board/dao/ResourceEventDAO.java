package hr.prism.board.dao;

import hr.prism.board.domain.*;
import hr.prism.board.repository.ActivityEventRepository;
import hr.prism.board.repository.ResourceEventSearchRepository;
import hr.prism.board.repository.UserRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static hr.prism.board.enums.ActivityEvent.VIEW;
import static hr.prism.board.enums.ResourceEvent.RESPONSE_EVENTS;
import static hr.prism.board.enums.ResourceEvent.RESPONSE_EVENT_STRINGS;
import static hr.prism.board.utils.BoardUtils.makeSoundex;
import static java.util.Collections.emptyList;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

@Repository
@Transactional
public class ResourceEventDAO {

    private final UserRepository userRepository;

    private final ResourceEventSearchRepository resourceEventSearchRepository;

    private final ActivityEventRepository activityEventRepository;

    private final EntityManager entityManager;

    @Inject
    public ResourceEventDAO(UserRepository userRepository, ResourceEventSearchRepository resourceEventSearchRepository,
                            ActivityEventRepository activityEventRepository, EntityManager entityManager) {
        this.userRepository = userRepository;
        this.resourceEventSearchRepository = resourceEventSearchRepository;
        this.activityEventRepository = activityEventRepository;
        this.entityManager = entityManager;
    }

    @SuppressWarnings("JpaQlInspection")
    public List<ResourceEvent> getResponses(User user, Resource resource, String searchTerm) {
        List<Long> userIds = userRepository.findByResourceAndEvents(resource, RESPONSE_EVENTS);
        if (userIds.isEmpty()) {
            return emptyList();
        }

        String search = UUID.randomUUID().toString();
        boolean searchTermApplied = searchTerm != null;
        if (searchTermApplied) {
            resourceEventSearchRepository.insertBySearch(
                search, LocalDateTime.now(), makeSoundex(searchTerm), userIds, RESPONSE_EVENT_STRINGS);
            entityManager.flush();
        }

        String statement =
            "select distinct resourceEvent " +
                "from ResourceEvent resourceEvent " +
                "left join resourceEvent.searches search on search.search = :search " +
                "where resourceEvent.resource = :resource " +
                "and resourceEvent.user.id in (:userIds) " +
                "and resourceEvent.event in (:events) ";
        if (searchTermApplied) {
            statement += "and search.id is not null ";
        }

        statement += "order by search.id, resourceEvent.id desc";
        List<ResourceEvent> resourceEvents = entityManager.createQuery(statement, ResourceEvent.class)
            .setParameter("search", search)
            .setParameter("resource", resource)
            .setParameter("userIds", userIds)
            .setParameter("events", RESPONSE_EVENTS)
            .getResultList();

        if (searchTermApplied) {
            resourceEventSearchRepository.deleteBySearch(search);
        }

        if (resourceEvents.isEmpty()) {
            return emptyList();
        }

        Map<Activity, ResourceEvent> activityEvents =
            resourceEvents.stream()
                .collect(toMap(ResourceEvent::getActivity, identity()));

        List<ActivityEvent> views =
            activityEventRepository.findByActivitiesAndUserAndEvent(activityEvents.keySet(), user, VIEW);
        views.forEach(view -> activityEvents.get(view.getActivity()).setViewed(true));

        return resourceEvents;
    }

}
