package hr.prism.board.service.event;

import hr.prism.board.domain.BoardEntity;
import hr.prism.board.event.ActivityEvent;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TestActivityEventService extends ActivityEventService {

    @Override
    public void publishEvent(Object source, Long resourceId, boolean stateChange, List<hr.prism.board.workflow.Activity> activities) {
        super.sendActivities(new ActivityEvent(source, resourceId, stateChange, activities));
    }

    @Override
    public void publishEvent(Object source, Long resourceId, BoardEntity entity) {
        super.sendActivities(new ActivityEvent(source, resourceId, entity.getClass(), entity.getId()));
    }

    @Override
    public void publishEvent(Object source, Long resourceId, BoardEntity entity, List<hr.prism.board.workflow.Activity> activities) {
        super.sendActivities(new ActivityEvent(source, resourceId, entity.getClass(), entity.getId(), activities));
    }

}
