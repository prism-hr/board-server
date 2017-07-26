package hr.prism.board.service;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import hr.prism.board.exception.BoardNotModifiedException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.representation.ActivityRepresentation;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.Collection;
import java.util.List;

@Service
public class UserActivityService {

    private SetMultimap<Long, DeferredResult<List<ActivityRepresentation>>> requests = Multimaps.synchronizedSetMultimap(HashMultimap.create());

    public Collection<Long> getUserIds() {
        return requests.keySet();
    }

    public void storeRequest(Long userId, DeferredResult<List<ActivityRepresentation>> request) {
        requests.put(userId, request);
    }

    public void processRequests(Long userId, List<ActivityRepresentation> result) {
        requests.get(userId).forEach(request -> {
            removeRequest(userId, request);
            request.setResult(result);
        });
    }

    public void processRequestTimeout(Long userId, DeferredResult<List<ActivityRepresentation>> request) {
        removeRequest(userId, request);
        request.setErrorResult(new BoardNotModifiedException(ExceptionCode.USER_ACTIVITY_NOT_MODIFIED));
    }

    protected void removeRequest(Long userId, DeferredResult<List<ActivityRepresentation>> request) {
        requests.remove(userId, request);
    }

}
