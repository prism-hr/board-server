package hr.prism.board.mapper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(MockitoJUnitRunner.class)
public class BoardMapperTest {

    @Mock
    private DepartmentMapper departmentMapper;

    @Mock
    private ResourceMapper resourceMapper;

    private BoardMapper boardMapper;

    @Before
    public void setUp() {
        boardMapper = new BoardMapper(departmentMapper, resourceMapper);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(departmentMapper, resourceMapper);
        reset(departmentMapper, resourceMapper);
    }

    @Test
    public void apply_success() {

    }

    @Test
    public void apply_successWhenNull() {
        assertNull(boardMapper.apply(null));
    }

    @Test
    public void applySmall_success() {

    }

    @Test
    public void applySmall_successWhenNull() {
        assertNull(boardMapper.applySmall(null));
    }

}
