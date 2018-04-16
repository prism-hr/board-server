package hr.prism.board.service;

import java.time.LocalDateTime;

public class TestScheduledService extends ScheduledService {

    public TestScheduledService(boolean schedulerOn, ActivityService activityService, PostService postService,
                                ResourceTaskService resourceTaskService, DepartmentService departmentService) {
        super(schedulerOn, activityService, postService, resourceTaskService, departmentService);
    }

    @Override
    public LocalDateTime getBaseline() {
        return LocalDateTime.of(2017, 9, 1, 9, 0, 0);
    }

}
