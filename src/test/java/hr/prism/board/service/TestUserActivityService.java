package hr.prism.board.service;

import com.google.common.collect.HashMultimap;
import hr.prism.board.representation.ActivityRepresentation;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.List;

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
    protected void removeRequest(Long userId, DeferredResult<List<ActivityRepresentation>> request) {
        super.removeRequest(userId, request);
        if (recording) {
            sentRequests.put(userId, request);
        }
    }

}
