package hr.prism.board.service.event;

import hr.prism.board.domain.BoardEntity;
import hr.prism.board.domain.ResourceEvent;
import hr.prism.board.domain.UserRole;
import hr.prism.board.event.ActivityEvent;
import hr.prism.board.workflow.Activity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TestActivityEventService extends ActivityEventService {

    @Override
    public void publishEvent(Object source, Long resourceId, List<Activity> activities) {
        super.sendActivities(new ActivityEvent(source, resourceId, activities));
    }

    @Override
    public void publishEvent(Object source, Long resourceId, BoardEntity entity, List<Activity> activities) {
        Class<? extends BoardEntity> entityClass = entity.getClass();
        if (entityClass == UserRole.class) {
            super.sendActivities(new ActivityEvent(source, resourceId, entity.getId(), null, activities));
        } else if (entityClass == ResourceEvent.class) {
            super.sendActivities(new ActivityEvent(source, resourceId, null, entity.getId(), activities));
        }
    }

    @Override
    public void publishEvent(Object source, Long resourceId, Long userRoleId) {
        super.sendActivities(new ActivityEvent(source, resourceId, userRoleId));
    }

}
