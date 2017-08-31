package hr.prism.board.service;

import com.google.common.collect.HashMultimap;
import hr.prism.board.api.UserApi;
import hr.prism.board.enums.Activity;
import hr.prism.board.enums.ResourceEvent;
import hr.prism.board.enums.Role;
import hr.prism.board.representation.ActivityRepresentation;
import hr.prism.board.representation.ResourceEventRepresentation;
import hr.prism.board.representation.UserRoleRepresentation;
import org.junit.Assert;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

import javax.inject.Inject;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TestUserActivityService extends UserActivityService {

    private boolean recording = false;

    private HashMultimap<Long, DeferredResult<List<ActivityRepresentation>>> sentRequests = HashMultimap.create();

    @Inject
    private UserApi userApi;

    public void record() {
        this.recording = true;
        this.sentRequests.clear();
    }

    public void stop() {
        this.recording = false;
    }

    @SuppressWarnings("unchecked")
    public Set<ActivityInstance> verify(Long userId, ActivityInstance... expectedActivityInstances) {
        Iterator<DeferredResult<List<ActivityRepresentation>>> iterator = sentRequests.removeAll(userId).iterator();
        Set<ActivityInstance> actualActivityInstances = ((List<ActivityRepresentation>) iterator.next().getResult())
            .stream().map(ActivityInstance::fromActivityRepresentation).collect(Collectors.toSet());

        Assert.assertEquals(expectedActivityInstances.length, actualActivityInstances.size());
        for (ActivityInstance expectedActivityInstance : expectedActivityInstances) {
            Assert.assertTrue(actualActivityInstances.contains(expectedActivityInstance));
        }

        userApi.refreshActivities(userId);
        return actualActivityInstances;
    }

    @Override
    public Set<DeferredResult<List<ActivityRepresentation>>> processRequests(Long userId, List<ActivityRepresentation> result) {
        Set<DeferredResult<List<ActivityRepresentation>>> userRequests = super.processRequests(userId, result);
        if (recording) {
            sentRequests.putAll(userId, userRequests);
        }

        return userRequests;
    }

    @Override
    public void processRequestTimeout(Long userId, DeferredResult<List<ActivityRepresentation>> request) {
        super.processRequestTimeout(userId, request);
        if (recording) {
            sentRequests.put(userId, request);
        }
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

        public static ActivityInstance fromActivityRepresentation(ActivityRepresentation activityRepresentation) {
            UserRoleRepresentation userRoleRepresentation = activityRepresentation.getUserRole();
            ResourceEventRepresentation resourceEventRepresentation = activityRepresentation.getResourceEvent();
            if (userRoleRepresentation == null && resourceEventRepresentation == null) {
                return new ActivityInstance(activityRepresentation.getResource().getId(), activityRepresentation.getActivity());
            } else if (userRoleRepresentation == null) {
                return new ActivityInstance(activityRepresentation.getResource().getId(),
                    resourceEventRepresentation.getUser().getId(), resourceEventRepresentation.getEvent(), activityRepresentation.getActivity());
            }

            return new ActivityInstance(activityRepresentation.getResource().getId(),
                userRoleRepresentation.getUser().getId(), userRoleRepresentation.getRole(), activityRepresentation.getActivity());
        }

        public Long getResourceId() {
            return resourceId;
        }

        public Long getUserId() {
            return userId;
        }

        public Role getRole() {
            return role;
        }

        public ResourceEvent getResourceEvent() {
            return resourceEvent;
        }

        public Activity getActivity() {
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
