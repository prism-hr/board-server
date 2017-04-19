package hr.prism.board.api;

import com.google.common.base.Joiner;
import hr.prism.board.domain.*;
import hr.prism.board.dto.BoardDTO;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.PostVisibility;
import hr.prism.board.representation.ActionRepresentation;
import hr.prism.board.representation.BoardRepresentation;
import hr.prism.board.representation.DepartmentRepresentation;
import hr.prism.board.service.BoardService;
import hr.prism.board.service.DepartmentService;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.stream.Collectors;

@Component
public class DepartmentBoardHelper {
    
    @Inject
    private DepartmentApi departmentApi;
    
    @Inject
    private BoardApi boardApi;
    
    @Inject
    private BoardService boardService;
    
    @Inject
    private DepartmentService departmentService;
    
    @Inject
    private UserRoleService userRoleService;
    
    public void verifyBoard(User user, BoardDTO boardDTO, BoardRepresentation boardR, boolean expectDepartmentAdministrator) {
        Assert.assertEquals(boardDTO.getName(), boardR.getName());
        Assert.assertEquals(boardDTO.getPurpose(), boardR.getPurpose());
        Assert.assertThat(boardR.getPostCategories(), Matchers.containsInAnyOrder(boardDTO.getPostCategories().toArray(new String[0])));
        Assert.assertEquals(PostVisibility.PART_PRIVATE, boardR.getDefaultPostVisibility());
        
        DepartmentRepresentation departmentR = boardR.getDepartment();
        Assert.assertEquals(boardDTO.getDepartment().getName(), departmentR.getName());
        Assert.assertThat(departmentR.getMemberCategories(), Matchers.containsInAnyOrder(boardDTO.getDepartment().getMemberCategories().toArray(new String[0])));
        
        Board board = boardService.getBoard(boardR.getId());
        Department department = departmentService.getDepartment(departmentR.getId());
        Assert.assertEquals(Joiner.on("/").join(department.getHandle(), boardR.getHandle()), board.getHandle());
        Assert.assertThat(boardR.getActions().stream().map(ActionRepresentation::getAction).collect(Collectors.toList()),
            Matchers.containsInAnyOrder(Action.VIEW, Action.EDIT, Action.EXTEND));
        
        Assert.assertThat(board.getParents().stream().map(ResourceRelation::getResource1).collect(Collectors.toList()), Matchers.containsInAnyOrder(board, department));
        Assert.assertTrue(userRoleService.hasUserRole(board, user, Role.ADMINISTRATOR));
        
        Assert.assertThat(department.getParents().stream().map(ResourceRelation::getResource1).collect(Collectors.toList()), Matchers.contains(department));
        if (expectDepartmentAdministrator) {
            Assert.assertTrue(userRoleService.hasUserRole(department, user, Role.ADMINISTRATOR));
            Assert.assertThat(boardR.getActions().stream().map(ActionRepresentation::getAction).collect(Collectors.toList()),
                Matchers.containsInAnyOrder(Action.VIEW, Action.EDIT, Action.EXTEND));
        }
    }
    
    public DepartmentRepresentation verifyGetDepartment(Long id) {
        DepartmentRepresentation departmentR = departmentApi.getDepartment(id);
        DepartmentRepresentation departmentRByHandle = departmentApi.getDepartmentByHandle(departmentR.getHandle());
        Assert.assertEquals(departmentR.getId(), departmentRByHandle.getId());
        return departmentRByHandle;
    }
    
    public BoardRepresentation verifyGetBoard(Long id) {
        BoardRepresentation boardR = boardApi.getBoard(id);
        BoardRepresentation boardRByHandle = boardApi.getBoardByHandle(Joiner.on("/").join(boardR.getDepartment().getHandle(), boardR.getHandle()));
        Assert.assertEquals(boardR.getId(), boardRByHandle.getId());
        return boardRByHandle;
    }
    
}
