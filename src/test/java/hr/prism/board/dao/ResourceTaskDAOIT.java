package hr.prism.board.dao;

import hr.prism.board.DbTestContext;
import hr.prism.board.domain.ResourceTask;
import hr.prism.board.repository.ResourceTaskRepository;
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
@Sql("classpath:data/resourceTaskDAO_setUp.sql")
@Sql(value = "classpath:data/resourceTaskDAO_tearDown.sql", executionPhase = AFTER_TEST_METHOD)
public class ResourceTaskDAOIT {

    @Inject
    private ResourceTaskDAO resourceTaskDAO;

    @Inject
    private ResourceTaskRepository resourceTaskRepository;

    @Test
    public void insertResourceTasks_successWhenCreateDepartment() {
        resourceTaskDAO.insertResourceTasks(1L, 1L, DEPARTMENT_TASKS);
        assertThat(
            resourceTaskRepository.findByResourceId(1L)
                .stream()
                .map(ResourceTask::getTask)
                .collect(toList()))
            .containsExactly(CREATE_MEMBER, CREATE_POST, DEPLOY_BADGE);
    }

}
