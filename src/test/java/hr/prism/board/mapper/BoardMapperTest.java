package hr.prism.board.mapper;

import com.google.common.collect.ImmutableSet;
import hr.prism.board.domain.Board;
import hr.prism.board.domain.Department;
import hr.prism.board.domain.ResourceCategory;
import hr.prism.board.representation.BoardRepresentation;
import hr.prism.board.representation.DepartmentRepresentation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static hr.prism.board.enums.CategoryType.POST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BoardMapperTest {

    @Mock
    private DepartmentMapper departmentMapper;

    @Mock
    private ResourceMapper resourceMapper;

    private BoardMapper boardMapper;

    private Department department;

    private Board board;

    private DepartmentRepresentation departmentRepresentation;

    @Before
    public void setUp() {
        department = new Department();
        department.setHandle("/university/department");

        board = new Board();
        board.setId(3L);
        board.setParent(department);
        board.setHandle("/university/department/board");

        departmentRepresentation = new DepartmentRepresentation();
        departmentRepresentation.setId(2L);

        when(resourceMapper.apply(board, BoardRepresentation.class)).thenReturn(new BoardRepresentation());
        when(resourceMapper.applySmall(board, BoardRepresentation.class)).thenReturn(new BoardRepresentation());
        when(resourceMapper.getHandle(board, department)).thenReturn("board");
        when(departmentMapper.applySmall(department)).thenReturn(departmentRepresentation);
        when(departmentMapper.applyMedium(department)).thenReturn(departmentRepresentation);

        boardMapper = new BoardMapper(departmentMapper, resourceMapper);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(departmentMapper, resourceMapper);
        reset(departmentMapper, resourceMapper);
    }

    @Test
    public void apply_success() {
        board.setCategories(ImmutableSet.of(
            new ResourceCategory().setType(POST).setName("Employment")));

        BoardRepresentation boardRepresentation = boardMapper.apply(board);

        assertEquals(departmentRepresentation, boardRepresentation.getDepartment());
        assertEquals("board", boardRepresentation.getHandle());
        assertThat(boardRepresentation.getPostCategories()).containsExactly("Employment");

        verify(resourceMapper, times(1)).apply(board, BoardRepresentation.class);
        verify(departmentMapper, times(1)).applySmall(department);
        verify(resourceMapper, times(1)).getHandle(board, department);
    }

    @Test
    public void apply_successWhenNull() {
        assertNull(boardMapper.apply(null));
    }

    @Test
    public void applySmall_success() {
        BoardRepresentation boardRepresentation = boardMapper.applySmall(board);

        assertEquals(departmentRepresentation, boardRepresentation.getDepartment());
        assertEquals("board", boardRepresentation.getHandle());

        verify(resourceMapper, times(1)).applySmall(board, BoardRepresentation.class);
        verify(departmentMapper, times(1)).applySmall(department);
        verify(resourceMapper, times(1)).getHandle(board, department);
    }

    @Test
    public void applySmall_successWhenNull() {
        assertNull(boardMapper.applySmall(null));
    }

    @Test
    public void applyMedium_success() {
        board.setCategories(ImmutableSet.of(
            new ResourceCategory().setType(POST).setName("Employment")));

        BoardRepresentation boardRepresentation = boardMapper.applyMedium(board);

        assertEquals(departmentRepresentation, boardRepresentation.getDepartment());
        assertEquals("board", boardRepresentation.getHandle());
        assertThat(boardRepresentation.getPostCategories()).containsExactly("Employment");

        verify(resourceMapper, times(1)).applySmall(board, BoardRepresentation.class);
        verify(departmentMapper, times(1)).applyMedium(department);
        verify(resourceMapper, times(1)).getHandle(board, department);
    }

    @Test
    public void applyMedium_successWhenNull() {
        assertNull(boardMapper.applySmall(null));
    }

}
