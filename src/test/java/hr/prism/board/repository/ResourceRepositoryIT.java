package hr.prism.board.repository;

import hr.prism.board.DbTestContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;

import static hr.prism.board.enums.Scope.DEPARTMENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

@DbTestContext
@RunWith(SpringRunner.class)
@Sql("classpath:data/resourceRepository_setUp.sql")
@Sql(value = "classpath:data/resourceRepository_tearDown.sql", executionPhase = AFTER_TEST_METHOD)
public class ResourceRepositoryIT {

    @Inject
    private ResourceRepository resourceRepository;

    @Test
    public void findHandleByLikeSuggestedHandle_successWhenDepartment() {
        assertThat(resourceRepository.findHandleLikeSuggestedHandle(DEPARTMENT, "university/department"))
            .containsExactly("university/department-2", "university/department");
    }

}
