package hr.prism.board.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.pusher.rest.Pusher;
import com.pusher.rest.data.PresenceUser;
import com.pusher.rest.data.Result;
import hr.prism.board.dao.ActivityDAO;
import hr.prism.board.domain.*;
import hr.prism.board.dto.PusherAuthenticationDTO;
import hr.prism.board.enums.Role;
import hr.prism.board.enums.Scope;
import hr.prism.board.exception.BoardForbiddenException;
import hr.prism.board.mapper.ActivityMapper;
import hr.prism.board.repository.ActivityEventRepository;
import hr.prism.board.repository.ActivityRepository;
import hr.prism.board.repository.ActivityRoleRepository;
import hr.prism.board.repository.ActivityUserRepository;
import hr.prism.board.representation.ActivityRepresentation;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static hr.prism.board.enums.ActivityEvent.DISMISSAL;
import static hr.prism.board.enums.ActivityEvent.VIEW;
import static hr.prism.board.exception.ExceptionCode.UNAUTHENTICATED_USER;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;

@Service
@Transactional
public class ActivityService {

    private static final Logger LOGGER = getLogger(ActivityService.class);

    private volatile Set<Long> userIds = ConcurrentHashMap.newKeySet();

    private final boolean pusherOn;

    private final ActivityRepository activityRepository;

    private final ActivityDAO activityDAO;

    private final ActivityRoleRepository activityRoleRepository;

    private final ActivityUserRepository activityUserRepository;

    private final ActivityEventRepository activityEventRepository;

    private final UserService userService;

    private final ActivityMapper activityMapper;

    private final Pusher pusher;

    private final ObjectMapper objectMapper;

    private final EntityManager entityManager;

    @Inject
    public ActivityService(@Value("${pusher.on}") boolean pusherOn, ActivityRepository activityRepository,
                           ActivityDAO activityDAO, ActivityRoleRepository activityRoleRepository,
                           ActivityUserRepository activityUserRepository,
                           ActivityEventRepository activityEventRepository, UserService userService,
                           ActivityMapper activityMapper, Pusher pusher, ObjectMapper objectMapper,
                           EntityManager entityManager) {
        this.pusherOn = pusherOn;
        this.activityRepository = activityRepository;
        this.activityDAO = activityDAO;
        this.activityRoleRepository = activityRoleRepository;
        this.activityUserRepository = activityUserRepository;
        this.activityEventRepository = activityEventRepository;
        this.userService = userService;
        this.activityMapper = activityMapper;
        this.pusher = pusher;
        this.objectMapper = objectMapper;
        this.entityManager = entityManager;
    }

    public Activity getByResourceActivityAndRole(Resource resource, hr.prism.board.enums.Activity activity,
                                                 Scope scope, Role role) {
        return activityRepository.findByResourceAndActivityAndRole(resource, activity, scope, role);
    }

    public List<ActivityRepresentation> getActivities(User user) {
        return getActivities(user.getId());
    }

    public List<ActivityRepresentation> getActivities(Long userId) {
        synchronized (this) {
            userIds.add(userId);
        }

        List<Activity> activities = activityDAO.getActivities(userId);
        if (activities.isEmpty()) {
            return emptyList();
        }

        List<Long> viewedActivityIds =
            activityRepository.findIdsByActivitiesAndUserIdAndEvent(activities, userId, VIEW);
        return activities.stream()
            .map(activity -> activity.setViewed(viewedActivityIds.contains(activity.getId())))
            .map(activityMapper)
            .collect(toList());
    }

    public Activity createOrUpdateActivity(Resource resource, hr.prism.board.enums.Activity activity) {
        Activity entity = activityRepository.findByResourceAndActivity(resource, activity);
        return entity == null ?
            createActivity(resource, null, null, activity) :
            activityRepository.save(entity);
    }

    public Activity createOrUpdateActivity(UserRole userRole, hr.prism.board.enums.Activity activity) {
        Activity entity = activityRepository.findByUserRoleAndActivity(userRole, activity);
        return entity == null ?
            createActivity(userRole.getResource(), userRole, null, activity) :
            activityRepository.save(entity);
    }

    public Activity createOrUpdateActivity(ResourceEvent resourceEvent, hr.prism.board.enums.Activity activity) {
        Activity entity = activityRepository.findByResourceEventAndActivity(resourceEvent, activity);
        return entity == null ?
            createActivity(resourceEvent.getResource(), null, resourceEvent, activity) :
            activityRepository.save(entity);
    }

    @SuppressWarnings("UnusedReturnValue")
    public ActivityRole getOrCreateActivityRole(Activity activity, Scope scope, Role role) {
        ActivityRole activityRole = activityRoleRepository.findByActivityAndScopeAndRole(activity, scope, role);
        if (activityRole == null) {
            activityRole = activityRoleRepository.save(
                new ActivityRole()
                    .setActivity(activity)
                    .setScope(scope)
                    .setRole(role));
        }

        return activityRole;
    }

    @SuppressWarnings("UnusedReturnValue")
    public ActivityUser getOrCreateActivityUser(Activity activity, User user) {
        ActivityUser activityUser = activityUserRepository.findByActivityAndUser(activity, user);
        if (activityUser == null) {
            activityUser = activityUserRepository.save(
                new ActivityUser()
                    .setActivity(activity)
                    .setUser(user));
        }

        return activityUser;
    }

    public void viewActivity(User user, Long activityId) {
        Activity activity = activityRepository.findOne(activityId);
        viewActivity(activity, user);
    }

    public void dismissActivity(User user, Long activityId) {
        hr.prism.board.domain.Activity activity = activityRepository.findOne(activityId);
        if (activity != null) {
            ActivityEvent activityEvent =
                activityEventRepository.findByActivityAndUserAndEvent(activity, user, DISMISSAL);
            if (activityEvent == null) {
                activityEventRepository.save(
                    new ActivityEvent()
                        .setActivity(activity)
                        .setUser(user)
                        .setEvent(DISMISSAL));
                sendActivities(user.getId());
            }
        }
    }

    public void deleteActivities(Resource resource) {
        activityDAO.deleteActivities(resource);
    }

    public void deleteActivities(UserRole userRole) {
        activityDAO.deleteActivities(userRole);
    }

    public void deleteActivities(Resource resource, User user, Role role) {
        activityDAO.deleteActivities(resource, user, role);
    }

    public void sendActivities(Resource resource) {
        entityManager.flush();
        List<Long> userIds = getUserIds();
        if (!userIds.isEmpty()) {
            for (Long userId : userService.getByResourceAndUserIds(resource, userIds)) {
                sendActivities(userId);
            }
        }
    }

    public void deleteActivities(Resource resource, List<hr.prism.board.enums.Activity> activities) {
        activityDAO.deleteActivities(resource, activities);
    }

    public List<ActivityEvent> findViews(Collection<Activity> activities, User user) {
        return activityEventRepository.findByActivitiesAndUserAndEvent(activities, user, VIEW);
    }

    public void viewActivity(Activity activity, User user) {
        ActivityEvent activityEvent = activityEventRepository.findByActivityAndUserAndEvent(activity, user, VIEW);
        if (activityEvent == null) {
            activityEventRepository.save(
                new ActivityEvent()
                    .setActivity(activity)
                    .setUser(user)
                    .setEvent(VIEW));
        }
    }

    public void deleteActivityUsers(User user) {
        activityUserRepository.deleteByUser(user);
    }

    public String authenticatePusher(User user, PusherAuthenticationDTO pusherAuthentication) {
        String channel = pusherAuthentication.getChannelName();
        String channelUserId = channel.split("-")[2];

        Long userId = user.getId();
        if (channelUserId.equals(userId.toString())) {
            LOGGER.info("Connecting user ID: " + userId + " to channel: " + channel);
            return pusher.authenticate(pusherAuthentication.getSocketId(), channel,
                new PresenceUser(userId,
                    ImmutableMap.of("name", user.getFullName(), "email", user.getEmailDisplay())));
        }

        throw new BoardForbiddenException(UNAUTHENTICATED_USER,
            "User ID: " + userId + " does not have permission to connect to channel: " + channel);
    }

    public void updateActivities() throws IOException {
        Result result = pusher.get("/channels");
        String message = result.getMessage();
        if (message != null) {
            Map<String, Map<String, Map<String, Object>>> wrapper = objectMapper.readValue(
                message, new TypeReference<Map<String, Map<String, Map<String, Object>>>>() {
                });

            Map<String, Map<String, Object>> channels = wrapper.get("channels");
            if (channels != null) {
                LOGGER.info("Updating users subscribed to activity stream");
                setUserIds(wrapper.get("channels").keySet().stream()
                    .filter(channel -> channel.startsWith("presence-activities-"))
                    .map(channel -> Long.parseLong(channel.split("-")[2])).collect(toList()));
            }
        }
    }

    public void sendActivities(Long userId, List<ActivityRepresentation> activities) {
        LOGGER.info("Sending " + activities.size() + " activities to user: " + userId);
        if (pusherOn) {
            pusher.trigger("presence-activities-" + userId, "activities", activities);
        }
    }

    private Activity createActivity(Resource resource, UserRole userRole, ResourceEvent resourceEvent,
                                    hr.prism.board.enums.Activity activity) {
        return activityRepository.save(
            new hr.prism.board.domain.Activity()
                .setResource(resource)
                .setUserRole(userRole)
                .setResourceEvent(resourceEvent)
                .setActivity(activity)
                .setFilterByCategory(activity.isFilterByCategory()));
    }

    private synchronized ImmutableList<Long> getUserIds() {
        return ImmutableList.copyOf(userIds);
    }

    private synchronized void setUserIds(List<Long> userIds) {
        this.userIds.clear();
        this.userIds.addAll(userIds);
    }

    private void sendActivities(Long userId) {
        entityManager.flush();
        sendActivities(userId, getActivities(userId));
    }

}
