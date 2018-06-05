package hr.prism.board.service;

import com.google.common.collect.ImmutableMap;
import hr.prism.board.DbTestContext;
import hr.prism.board.domain.University;
import hr.prism.board.exception.BoardNotFoundException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;

import static hr.prism.board.enums.Scope.UNIVERSITY;
import static hr.prism.board.exception.ExceptionCode.MISSING_RESOURCE;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

@DbTestContext
@RunWith(SpringRunner.class)
@Sql(scripts = {"classpath:data/tearDown.sql", "classpath:data/universityService_setUp.sql"})
@Sql(scripts = {"classpath:data/tearDown.sql"}, executionPhase = AFTER_TEST_METHOD)
public class UniversityServiceIT {

    @Inject
    private UniversityService universityService;

    @Test
    public void getById_success() {
        University university = universityService.getById(1L);
        assertEquals(1L, university.getId().longValue());
    }

    @Test
    public void getById_failure() {
        assertThatThrownBy(() -> universityService.getById(0L))
            .isExactlyInstanceOf(BoardNotFoundException.class)
            .hasFieldOrPropertyWithValue("exceptionCode", MISSING_RESOURCE)
            .hasFieldOrPropertyWithValue("properties",
                ImmutableMap.of("scope", UNIVERSITY, "id", 0L));
    }

}
