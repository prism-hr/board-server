package hr.prism.board.service;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import hr.prism.board.exception.BoardNotModifiedException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.representation.ActivityRepresentation;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.List;
import java.util.Set;

@Service
public class UserActivityService {
    
    private volatile HashMultimap<Long, DeferredResult<List<ActivityRepresentation>>> requests = HashMultimap.create();
    
    public synchronized List<Long> getUserIds() {
        return ImmutableList.copyOf(requests.keySet());
    }

    public void storeRequest(Long userId, DeferredResult<List<ActivityRepresentation>> request) {
        requests.put(userId, request);
    }

    public Set<DeferredResult<List<ActivityRepresentation>>> processRequests(Long userId, List<ActivityRepresentation> result) {
        Set<DeferredResult<List<ActivityRepresentation>>> userRequests = getUserRequests(userId);
        userRequests.forEach(userRequest -> userRequest.setResult(result));
        return userRequests;
    }
    
    public synchronized void processRequestTimeout(Long userId, DeferredResult<List<ActivityRepresentation>> request) {
        requests.remove(userId, request);
        request.setErrorResult(new BoardNotModifiedException(ExceptionCode.USER_ACTIVITY_NOT_MODIFIED, "No new activities for user: " + userId));
    }
    
    private synchronized Set<DeferredResult<List<ActivityRepresentation>>> getUserRequests(Long userId) {
        return requests.removeAll(userId);
    }

}
