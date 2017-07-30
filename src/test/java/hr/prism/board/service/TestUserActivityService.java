package hr.prism.board.service;

import com.google.common.collect.HashMultimap;
import hr.prism.board.enums.Activity;
import hr.prism.board.enums.Role;
import hr.prism.board.representation.ActivityRepresentation;
import org.junit.Assert;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TestUserActivityService extends UserActivityService {

    private boolean recording = false;

    private HashMultimap<Long, DeferredResult<List<ActivityRepresentation>>> sentRequests = HashMultimap.create();

    public void record() {
        this.recording = true;
    }

    public void stop() {
        this.sentRequests.clear();
        this.recording = false;
    }

    @SuppressWarnings("unchecked")
    public Set<ActivityInstance> verify(Long userId, ActivityInstance... expectedActivityInstances) {
        if (expectedActivityInstances == null) {
            Assert.assertEquals(0, ((List<ActivityRepresentation>) sentRequests.get(userId).iterator().next().getResult()).size());
            return Collections.emptySet();
        } else {
            Set<ActivityInstance> actualActivityInstances = ((List<ActivityRepresentation>) sentRequests.removeAll(userId).iterator().next().getResult())
                .stream().map(representation ->
                    new ActivityInstance(
                        representation.getId(),
                        representation.getResource().getId(),
                        representation.getUserRole().getUser().getId(),
                        representation.getUserRole().getRole(),
                        representation.getActivity()))
                .collect(Collectors.toSet());

            Assert.assertEquals(expectedActivityInstances.length, actualActivityInstances.size());
            for (ActivityInstance expectedActivityInstance : expectedActivityInstances) {
                Assert.assertTrue(actualActivityInstances.contains(expectedActivityInstance));
            }

            return actualActivityInstances;
        }
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

        private Long id;

        private Long resourceId;

        private Long userId;

        private Role role;

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

        public ActivityInstance(Long id, Long resourceId, Long userId, Role role, Activity activity) {
            this.id = id;
            this.resourceId = resourceId;
            this.userId = userId;
            this.role = role;
            this.activity = activity;
        }

        public Long getId() {
            return id;
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
                && Objects.equals(activity, that.getActivity());
        }

    }

}
