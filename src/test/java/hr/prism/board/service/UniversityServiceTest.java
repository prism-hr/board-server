package hr.prism.board.service;

import hr.prism.board.dao.UniversityDAO;
import hr.prism.board.domain.University;
import hr.prism.board.exception.BoardNotFoundException;
import hr.prism.board.repository.UniversityRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static io.jsonwebtoken.lang.Assert.notNull;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UniversityServiceTest {

    @Mock
    private UniversityRepository universityRepository;

    @Mock
    private UniversityDAO universityDAO;

    @Mock
    private ResourceService resourceService;

    private UniversityService universityService;

    @Before
    public void setUp() {
        universityService = new UniversityService(universityRepository, universityDAO, resourceService);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(universityRepository, universityDAO, resourceService);
        reset(universityRepository, universityDAO, resourceService);
    }

    @Test
    public void getByIdWithExistenceCheck_success() {
        when(resourceService.getById(1L)).thenReturn(new University());
        notNull(universityService.getByIdWithExistenceCheck(1L));
        verify(resourceService, times(1)).getById(1L);
    }

    @Test
    public void getByIdWithExistenceCheck_failureWhenDoesNotExist() {
        assertThatThrownBy(() -> universityService.getByIdWithExistenceCheck(1L))
            .isExactlyInstanceOf(BoardNotFoundException.class)
            .hasMessage("MISSING_RESOURCE: UNIVERSITY ID: 1 does not exist");
        verify(resourceService, times(1)).getById(1L);
    }

}
