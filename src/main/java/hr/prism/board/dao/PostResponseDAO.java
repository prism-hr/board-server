package hr.prism.board.dao;

import com.google.common.collect.HashMultimap;
import hr.prism.board.domain.Post;
import hr.prism.board.domain.ResourceEvent;
import hr.prism.board.domain.User;
import hr.prism.board.repository.ResourceEventRepository;
import hr.prism.board.repository.ResourceEventSearchRepository;
import hr.prism.board.repository.UserRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.*;

import static hr.prism.board.enums.ResourceEvent.RESPONSE;
import static hr.prism.board.enums.ResourceEvent.RESPONSE_EVENTS;
import static hr.prism.board.utils.BoardUtils.makeSoundex;
import static java.util.Collections.emptyList;

@Repository
@Transactional
public class PostResponseDAO {

    private final UserRepository userRepository;

    private final ResourceEventRepository resourceEventRepository;

    private final ResourceEventSearchRepository resourceEventSearchRepository;

    private final EntityManager entityManager;

    @
    @Inject
    public PostResponseDAO(UserRepository userRepository, ResourceEventRepository resourceEventRepository,
                           ResourceEventSearchRepository resourceEventSearchRepository, EntityManager entityManager) {
        this.userRepository = userRepository;
        this.resourceEventRepository = resourceEventRepository;
        this.resourceEventSearchRepository = resourceEventSearchRepository;
        this.entityManager = entityManager;
    }

    @SuppressWarnings("JpaQlInspection")
    public List<ResourceEvent> getPostResponses(Post post, String searchTerm) {
        List<Long> userIds = userRepository.findByResourceAndEvents(post, RESPONSE_EVENTS);
        if (userIds.isEmpty()) {
            return emptyList();
        }

        String search = UUID.randomUUID().toString();
        boolean searchTermApplied = searchTerm != null;
        if (searchTermApplied) {
            resourceEventSearchRepository.insertBySearch(search, LocalDateTime.now(), makeSoundex(searchTerm), userIds);
            entityManager.flush();
        }

        String statement =
            "select distinct resourceEvent " +
                "from ResourceEvent resourceEvent " +
                "left join resourceEvent.searches search on search.search = :search " +
                "where resourceEvent.resource = :post " +
                "and resourceEvent.user.id in (:userIds) ";
        if (searchTermApplied) {
            statement += "and search.id is not null ";
        }

        statement += "order by search.id, resourceEvent.id desc";
        List<ResourceEvent> resourceEvents = entityManager.createQuery(statement, ResourceEvent.class)
            .setParameter("search", search)
            .setParameter("post", post)
            .setParameter("userIds", userIds)
            .getResultList();

        if (searchTermApplied) {
            resourceEventSearchRepository.deleteBySearch(search);
        }

        if (resourceEvents.isEmpty()) {
            return emptyList();
        }

        HashMultimap<String, User> userIpAddresses = HashMultimap.create();
        Map<User, ResourceEvent> userResourceEvents = new LinkedHashMap<>();

        resourceEvents.forEach(resourceEvent -> {
            User resourceEventUser = resourceEvent.getUser();
            ResourceEvent headResourceEvent = userResourceEvents.get(resourceEventUser);
            if (headResourceEvent == null) {
                userResourceEvents.put(resourceEventUser, resourceEvent);
            } else if (resourceEvent.getEvent() == RESPONSE || resourceEvent.hasDemographicData()) {
                userResourceEvents.put(resourceEventUser, resourceEvent);
                List<ResourceEvent> resourceEventHistory = new ArrayList<>();
                resourceEventHistory.add(headResourceEvent);

                List<ResourceEvent> previousResourceEventHistory = headResourceEvent.getHistory();
                if (previousResourceEventHistory != null) {
                    resourceEventHistory.addAll(previousResourceEventHistory);
                }

                resourceEvent.setHistory(resourceEventHistory);
            } else {
                appendToResourceEventHistory(headResourceEvent, resourceEvent);
            }

            String ipAddress = resourceEvent.getIpAddress();
            if (ipAddress != null) {
                userIpAddresses.put(ipAddress, resourceEventUser);
            }
        });

        if (!userIpAddresses.isEmpty()) {
            resourceEventService.findByIpAddresses(userIpAddresses.keySet()).forEach(resourceEvent ->
                userIpAddresses.get(resourceEvent.getIpAddress()).forEach(resourceEventUser ->
                    appendToResourceEventHistory(userResourceEvents.get(resourceEventUser), resourceEvent)));
        }

        Collection<ResourceEvent> headResourceEvents = userResourceEvents.values();
        Map<hr.prism.board.domain.Activity, ResourceEvent> indexByActivities = new HashMap<>();
        for (ResourceEvent headResourceEvent : headResourceEvents) {
            headResourceEvent.setExposeResponseData(headResourceEvent.getUser().equals(user));

            hr.prism.board.domain.Activity activity = headResourceEvent.getActivity();
            if (activity != null) {
                indexByActivities.put(activity, headResourceEvent);
            }
        }

        if (!indexByActivities.isEmpty()) {
            for (hr.prism.board.domain.ActivityEvent activityEvent : activityService.findViews(indexByActivities.keySet(), user)) {
                indexByActivities.get(activityEvent.getActivity()).setViewed(true);
            }
        }

        return headResourceEvents;
    }

}
