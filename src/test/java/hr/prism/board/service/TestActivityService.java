package hr.prism.board.service;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import hr.prism.board.enums.Activity;
import hr.prism.board.enums.ResourceEvent;
import hr.prism.board.enums.Role;
import hr.prism.board.representation.ActivityRepresentation;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TestActivityService extends ActivityService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActivityService.class);

    private boolean recording = false;

    private ArrayListMultimap<Long, List<ActivityRepresentation>> sentActivities = ArrayListMultimap.create();

    public void record() {
        this.recording = true;
        this.sentActivities.clear();
        userIds.clear();
    }

    public void stop() {
        this.recording = false;
    }

    @SuppressWarnings("unchecked")
    public void verify(Long userId, ActivityInstance... expectedActivityInstances) {
        List<List<ActivityRepresentation>> removed = sentActivities.removeAll(userId);
        List<ActivityRepresentation> activityRepresentations = Iterables.getLast(removed);
        LOGGER.info("Checking activities for user: " + userId + " - " +
            activityRepresentations.stream().map(ActivityRepresentation::getId).map(Objects::toString).collect(Collectors.joining(", ")));

        Set<ActivityInstance> actualActivityInstances = activityRepresentations
            .stream().map(ActivityInstance::fromActivityRepresentation).collect(Collectors.toSet());

        Assert.assertEquals(expectedActivityInstances.length, actualActivityInstances.size());
        for (ActivityInstance expectedActivityInstance : expectedActivityInstances) {
            Assert.assertTrue(actualActivityInstances.contains(expectedActivityInstance));
        }
    }

    @Override
    public void sendActivities(Long userId, List<ActivityRepresentation> activities) {
        if (recording) {
            LOGGER.info("updating activities for user: " + userId + " - " + activities.stream()
                .map(ActivityRepresentation::getId).map(Objects::toString).collect(Collectors.joining(", ")));
            sentActivities.put(userId, activities);
        }

        super.sendActivities(userId, activities);
    }

    public static class ActivityInstance {

        private Long resourceId;

        private Long userId;

        private Role role;

        private ResourceEvent resourceEvent;

        private Activity activity;

        public ActivityInstance(Long resourceId, Activity activity) {
            this.resourceId = resourceId;
            this.activity = activity;
        }

        public ActivityInstance(Long resourceId, Long userId, Role role, Activity activity) {
            this.resourceId = resourceId;
            this.userId = userId;
            this.role = role;
            this.activity = activity;
        }

        public ActivityInstance(Long resourceId, Long userId, ResourceEvent resourceEvent, Activity activity) {
            this.resourceId = resourceId;
            this.userId = userId;
            this.resourceEvent = resourceEvent;
            this.activity = activity;
        }

        static ActivityInstance fromActivityRepresentation(ActivityRepresentation activityRepresentation) {
            Long userId = activityRepresentation.getUserId();
            Role role = activityRepresentation.getRole();
            if (userId == null && role == null) {
                return new ActivityInstance(activityRepresentation.getResourceId(), activityRepresentation.getActivity());
            } else if (role == null) {
                return new ActivityInstance(activityRepresentation.getResourceId(),
                    userId, activityRepresentation.getEvent(), activityRepresentation.getActivity());
            }

            return new ActivityInstance(
                activityRepresentation.getResourceId(), userId, role, activityRepresentation.getActivity());
        }

        Long getResourceId() {
            return resourceId;
        }

        Long getUserId() {
            return userId;
        }

        Role getRole() {
            return role;
        }

        ResourceEvent getResourceEvent() {
            return resourceEvent;
        }

        Activity getActivity() {
            return activity;
        }

        @Override
        public int hashCode() {
            return Objects.hash(resourceId, userId, role, activity);
        }

        @Override
        public boolean equals(Object object) {
            if (object == null || object.getClass() != getClass()) {
                return false;
            }

            ActivityInstance that = (ActivityInstance) object;
            return Objects.equals(resourceId, that.getResourceId()) && Objects.equals(userId, that.getUserId()) && Objects.equals(role, that.getRole())
                && Objects.equals(resourceEvent, that.getResourceEvent()) && Objects.equals(activity, that.getActivity());
        }

    }

}
