package hr.prism.board.service;

import hr.prism.board.enums.Activity;
import hr.prism.board.enums.ResourceEvent;
import hr.prism.board.enums.Role;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static org.junit.Assert.fail;

@Service
public class TestActivityService {

    @SuppressWarnings({"unchecked", "unused"})
    public void verify(Long userId, ActivityInstance... expectedActivityInstances) {
        fail("Replace this shit with proper assertions against the pusher mock");
//        List<List<ActivityRepresentation>> removed = sentActivities.removeAll(userId);
//        List<ActivityRepresentation> activityRepresentations = Iterables.getLast(removed);
//        LOGGER.info("Checking activities for user: " + userId + " - " +
//            activityRepresentations.stream().map(ActivityRepresentation::getId).map(Objects::toString).collect(Collectors.joining(", ")));
//
//        Set<ActivityInstance> actualActivityInstances = activityRepresentations
//            .stream().map(ActivityInstance::fromActivityRepresentation).collect(Collectors.toSet());
//
//        Assert.assertEquals(expectedActivityInstances.length, actualActivityInstances.size());
//        for (ActivityInstance expectedActivityInstance : expectedActivityInstances) {
//            Assert.assertTrue(actualActivityInstances.contains(expectedActivityInstance));
//        }
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

        @SuppressWarnings("WeakerAccess")
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
