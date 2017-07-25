package hr.prism.board.service.event;

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
    public void publishEvent(Object source, Long resourceId, Long userRoleId, List<Activity> activities) {
        super.sendActivities(new ActivityEvent(source, resourceId, userRoleId, activities));
    }

}
