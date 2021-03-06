package hr.prism.board.service;

import hr.prism.board.DbTestContext;
import hr.prism.board.domain.Department;
import hr.prism.board.domain.ResourceTask;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;

import static hr.prism.board.enums.ResourceTask.*;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

@DbTestContext
@RunWith(SpringRunner.class)
@Sql(scripts = {"classpath:data/tearDown.sql", "classpath:data/resourceTaskService_setUp.sql"})
@Sql(scripts = {"classpath:data/tearDown.sql"}, executionPhase = AFTER_TEST_METHOD)
public class ResourceTaskServiceIT {

    @Inject
    private ResourceTaskService resourceTaskService;

    @Test
    public void createForNewResource_success() {
        resourceTaskService.createForNewResource(1L, 1L, DEPARTMENT_TASKS);
        assertThat(resourceTaskService.getByResourceId(1L)
            .stream()
            .map(ResourceTask::getTask)
            .collect(toList()))
            .containsExactly(CREATE_MEMBER, CREATE_POST, DEPLOY_BADGE);
    }

    @Test
    public void completeTasks_success() {
        Department department = new Department();
        department.setId(2L);

        resourceTaskService.completeTasks(department, POST_TASKS);
        assertThat(resourceTaskService.getByResource(department)).containsExactly(CREATE_MEMBER);
    }

}
