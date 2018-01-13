package hr.prism.board.service;

import com.google.common.collect.ImmutableList;
import com.pusher.rest.Pusher;
import hr.prism.board.domain.*;
import hr.prism.board.enums.CategoryType;
import hr.prism.board.enums.Role;
import hr.prism.board.enums.Scope;
import hr.prism.board.enums.State;
import hr.prism.board.mapper.ActivityMapper;
import hr.prism.board.repository.ActivityEventRepository;
import hr.prism.board.repository.ActivityRepository;
import hr.prism.board.repository.ActivityRoleRepository;
import hr.prism.board.repository.ActivityUserRepository;
import hr.prism.board.representation.ActivityRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@SuppressWarnings({"SpringAutowiredFieldsWarningInspection", "UnusedReturnValue", "WeakerAccess"})
public class ActivityService {

    volatile Set<Long> userIds = new LinkedHashSet<>();

    private static final Logger LOGGER = LoggerFactory.getLogger(ActivityService.class);

    private static final String USER_ACTIVITY =
        "select distinct activity " +
            "from Activity activity " +
            "left join activity.activityRoles activityRole " +
            "left join activity.resource resource " +
            "left join resource.parents parentRelation " +
            "left join parentRelation.resource1 parent " +
            "left join parent.userRoles userRole " +
            "left join resource.categories resourceCategory " +
            "left join activity.activityUsers activityUser " +
            "where (activityUser.id is null " +
            "and activityRole.scope = parent.scope " +
            "and activityRole.role = userRole.role " +
            "and userRole.user.id = :userId " +
            "and userRole.state in (:userRoleStates) " +
            "and (activity.filterByCategory = false " +
            "or resourceCategory.id is null " +
            "or resourceCategory.type = :categoryType and resourceCategory.name = userRole.memberCategory) " +
            "or activityUser.user.id = :userId) " +
            "and activity.id not in (" +
            "select activityEvent.activity.id " +
            "from ActivityEvent activityEvent " +
            "where activityEvent.user.id = :userId " +
            "and activityEvent.event = :activityEvent) " +
            "order by activity.updatedTimestamp desc, activity.id desc";

    @Value("${pusher.on}")
    private boolean pusherOn;

    @Inject
    private ActivityRepository activityRepository;

    @Inject
    private ActivityRoleRepository activityRoleRepository;

    @Inject
    private ActivityUserRepository activityUserRepository;

    @Inject
    private ActivityEventRepository activityEventRepository;

    @Inject
    private UserService userService;

    @Inject
    private ActivityMapper activityMapper;

    @Lazy
    @Inject
    private Pusher pusher;

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private PlatformTransactionManager platformTransactionManager;

    public Activity findByResourceAndActivity(Resource resource, hr.prism.board.enums.Activity activity) {
        return activityRepository.findByResourceAndActivity(resource, activity);
    }

    public Activity findByUserRoleAndActivity(UserRole userRole, hr.prism.board.enums.Activity activity) {
        return activityRepository.findByUserRoleAndActivity(userRole, activity);
    }

    public List<ActivityRepresentation> getActivities() {
        Long userId = userService.getCurrentUserSecured().getId();
        synchronized (this) {
            userIds.add(userId);
        }

        return getActivities(userId);
    }

    public List<ActivityRepresentation> getActivities(Long userId) {
        List<Activity> activities = new TransactionTemplate(platformTransactionManager).execute(status ->
            entityManager.createQuery(USER_ACTIVITY, Activity.class)
                .setParameter("userId", userId)
                .setParameter("userRoleStates", State.ACTIVE_USER_ROLE_STATES)
                .setParameter("categoryType", CategoryType.MEMBER)
                .setParameter("activityEvent", hr.prism.board.enums.ActivityEvent.DISMISSAL)
                .setMaxResults(25)
                .getResultList());

        if (activities.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> viewedActivityIds = activityRepository.findIdsByActivitiesAndUserIdAndEvent(activities, userId, hr.prism.board.enums.ActivityEvent.VIEW);
        return activities.stream()
            .map(activity -> activity.setViewed(viewedActivityIds.contains(activity.getId())))
            .map(activityMapper)
            .collect(Collectors.toList());
    }

    public Activity createOrUpdateActivity(Resource resource, hr.prism.board.enums.Activity activity) {
        Activity entity = activityRepository.findByResourceAndActivity(resource, activity);
        return entity == null ?
            createActivity(resource, null, null, activity) :
            activityRepository.update(entity);
    }

    public Activity createOrUpdateActivity(UserRole userRole, hr.prism.board.enums.Activity activity) {
        Activity entity = activityRepository.findByUserRoleAndActivity(userRole, activity);
        return entity == null ?
            createActivity(userRole.getResource(), userRole, null, activity) :
            activityRepository.update(entity);
    }

    public Activity createOrUpdateActivity(ResourceEvent resourceEvent, hr.prism.board.enums.Activity activity) {
        Activity entity = activityRepository.findByResourceEventAndActivity(resourceEvent, activity);
        return entity == null ?
            createActivity(resourceEvent.getResource(), null, resourceEvent, activity) :
            activityRepository.update(entity);
    }

    public ActivityRole getOrCreateActivityRole(Activity activity, Scope scope, Role role) {
        ActivityRole activityRole = activityRoleRepository.findByActivityAndScopeAndRole(activity, scope, role);
        if (activityRole == null) {
            activityRole = activityRoleRepository.save(new ActivityRole().setActivity(activity).setScope(scope).setRole(role));
        }

        return activityRole;
    }

    public ActivityUser getOrCreateActivityUser(Activity activity, User user) {
        ActivityUser activityUser = activityUserRepository.findByActivityAndUser(activity, user);
        if (activityUser == null) {
            activityUser = activityUserRepository.save(new ActivityUser().setActivity(activity).setUser(user));
        }

        return activityUser;
    }

    public void viewActivity(Long activityId) {
        User user = userService.getCurrentUserSecured();
        Activity activity = activityRepository.findOne(activityId);
        viewActivity(activity, user);
    }

    public void dismissActivity(Long activityId) {
        User user = userService.getCurrentUserSecured();
        hr.prism.board.domain.Activity activity = activityRepository.findOne(activityId);
        if (activity != null) {
            ActivityEvent activityEvent = activityEventRepository.findByActivityAndUserAndEvent(activity, user, hr.prism.board.enums.ActivityEvent.DISMISSAL);
            if (activityEvent == null) {
                activityEventRepository.save(new ActivityEvent().setActivity(activity).setUser(user).setEvent(hr.prism.board.enums.ActivityEvent.DISMISSAL));
                sendActivities(user.getId());
            }
        }
    }

    public void deleteActivities(Resource resource) {
        activityEventRepository.deleteByResource(resource);
        activityRoleRepository.deleteByResource(resource);

        List<Long> ignores = activityRepository.findByResourceWithActivityUsers(resource);
        if (ignores.isEmpty()) {
            activityRepository.deleteByResource(resource);
        } else {
            activityRepository.deleteByResourceWithIgnores(resource, ignores);
        }
    }

    public void deleteActivities(UserRole userRole) {
        activityEventRepository.deleteByUserRole(userRole);
        activityRoleRepository.deleteByUserRole(userRole);
        activityRepository.deleteByUserRole(userRole);
    }

    public void deleteActivities(Resource resource, User user) {
        activityEventRepository.deleteByResourceAndUser(resource, user);
        activityRoleRepository.deleteByResourceAndUser(resource, user);
        activityRepository.deleteByResourceAndUser(resource, user);
    }

    public void deleteActivities(Resource resource, User user, Role role) {
        activityEventRepository.deleteByResourceAndUserAndRole(resource, user, role);
        activityRoleRepository.deleteByResourceAndUserAndRole(resource, user, role);
        activityRepository.deleteByResourceAndUserAndRole(resource, user, role);
    }

    public void deleteActivities(List<UserRole> userRoles) {
        activityEventRepository.deleteByUserRoles(userRoles);
        activityRoleRepository.deleteByUserRoles(userRoles);
        activityRepository.deleteByUserRoles(userRoles);
    }

    public ImmutableList<Long> getUserIds() {
        return ImmutableList.copyOf(userIds);
    }

    public synchronized void setUserIds(List<Long> userIds) {
        this.userIds.clear();
        this.userIds.addAll(userIds);
    }

    public void sendActivities(Long userId) {
        entityManager.flush();
        sendActivities(userId, getActivities(userId));
    }

    public void sendActivities(Resource resource) {
        entityManager.flush();
        List<Long> userIds = getUserIds();
        if (!userIds.isEmpty()) {
            for (Long userId : userService.findByResourceAndUserIds(resource, userIds)) {
                sendActivities(userId);
            }
        }
    }

    public void dismissActivities(Long resourceId, List<hr.prism.board.enums.Activity> activities, Long userId) {
        activityEventRepository.insertByResourceIdActivitiesUserIdAndEvent(resourceId,
            activities.stream().map(hr.prism.board.enums.Activity::name).collect(Collectors.toList()), userId,
            hr.prism.board.enums.ActivityEvent.DISMISSAL.name(), LocalDateTime.now());
    }

    public void deleteActivities(Resource resource, List<hr.prism.board.enums.Activity> activities) {
        activityEventRepository.deleteByResourceAndActivities(resource, activities);
        activityRoleRepository.deleteByResourceAndActivities(resource, activities);
        activityRepository.deleteByResourceAndActivities(resource, activities);
    }

    public List<ActivityEvent> findViews(Collection<Activity> activities, User user) {
        return activityEventRepository.findByActivitiesAndUserAndEvent(activities, user, hr.prism.board.enums.ActivityEvent.VIEW);
    }

    public void viewActivity(Activity activity, User user) {
        ActivityEvent activityEvent = activityEventRepository.findByActivityAndUserAndEvent(activity, user, hr.prism.board.enums.ActivityEvent.VIEW);
        if (activityEvent == null) {
            activityEventRepository.save(new ActivityEvent().setActivity(activity).setUser(user).setEvent(hr.prism.board.enums.ActivityEvent.VIEW));
        }
    }

    public void deleteActivityUsers(User user) {
        activityUserRepository.deleteByUser(user);
    }

    public void sendActivities(Long userId, List<ActivityRepresentation> activities) {
        LOGGER.info("Sending " + activities.size() + " activities to user: " + userId);
        if (pusherOn) {
            pusher.trigger("presence-activities-" + userId, "activities", activities);
        }
    }

    private Activity createActivity(Resource resource, UserRole userRole, ResourceEvent resourceEvent, hr.prism.board.enums.Activity activity) {
        return activityRepository.save(
            new hr.prism.board.domain.Activity()
                .setResource(resource)
                .setUserRole(userRole)
                .setResourceEvent(resourceEvent)
                .setActivity(activity)
                .setFilterByCategory(activity.isFilterByCategory()));
    }

}
