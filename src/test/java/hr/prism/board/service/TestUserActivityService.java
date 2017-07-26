package hr.prism.board.service;

import com.google.common.collect.HashMultimap;
import hr.prism.board.representation.ActivityRepresentation;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.List;
import java.util.Set;

@Service
public class TestUserActivityService extends UserActivityService {

    private boolean recording = false;

    private HashMultimap<Long, DeferredResult<List<ActivityRepresentation>>> sentRequests = HashMultimap.create();

    public void record() {
        this.recording = true;
    }

    public void stop() {
        this.recording = false;
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

}
