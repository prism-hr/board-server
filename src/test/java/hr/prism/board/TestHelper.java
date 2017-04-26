package hr.prism.board;

import com.google.common.base.Joiner;
import hr.prism.board.api.BoardApi;
import hr.prism.board.api.DepartmentApi;
import hr.prism.board.domain.*;
import hr.prism.board.dto.BoardDTO;
import hr.prism.board.dto.LocationDTO;
import hr.prism.board.dto.PostDTO;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.ExistingRelation;
import hr.prism.board.enums.PostVisibility;
import hr.prism.board.representation.*;
import hr.prism.board.service.BoardService;
import hr.prism.board.service.DepartmentService;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.stream.Collectors;

@Component
public class TestHelper {
    
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
        Assert.assertEquals(boardDTO.getDescription(), boardR.getDescription());
        Assert.assertThat(boardR.getPostCategories(), Matchers.containsInAnyOrder(boardDTO.getPostCategories().toArray(new String[0])));
        Assert.assertEquals(PostVisibility.PART_PRIVATE, boardR.getDefaultPostVisibility());
        
        DepartmentRepresentation departmentR = boardR.getDepartment();
        Assert.assertEquals(boardDTO.getDepartment().getName(), departmentR.getName());
        Assert.assertThat(departmentR.getMemberCategories(), Matchers.containsInAnyOrder(boardDTO.getDepartment().getMemberCategories().toArray(new String[0])));
        
        Board board = boardService.getBoard(boardR.getId());
        Department department = departmentService.getDepartment(departmentR.getId());
        Assert.assertEquals(Joiner.on("/").join(department.getHandle(), boardR.getHandle()), board.getHandle());
        Assert.assertThat(boardR.getActions().stream().map(ActionRepresentation::getAction).collect(Collectors.toList()),
            Matchers.containsInAnyOrder(Action.VIEW, Action.EDIT, Action.AUDIT, Action.EXTEND));
        
        Assert.assertThat(board.getParents().stream().map(ResourceRelation::getResource1).collect(Collectors.toList()), Matchers.containsInAnyOrder(board, department));
        Assert.assertTrue(userRoleService.hasUserRole(board, user, Role.ADMINISTRATOR));
        
        Assert.assertThat(department.getParents().stream().map(ResourceRelation::getResource1).collect(Collectors.toList()), Matchers.contains(department));
        if (expectDepartmentAdministrator) {
            Assert.assertTrue(userRoleService.hasUserRole(department, user, Role.ADMINISTRATOR));
            Assert.assertThat(boardR.getActions().stream().map(ActionRepresentation::getAction).collect(Collectors.toList()),
                Matchers.containsInAnyOrder(Action.VIEW, Action.EDIT, Action.AUDIT, Action.EXTEND));
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
    
    public void verifyUser(User user, UserRepresentation userRepresentation) {
        Assert.assertEquals(user.getId(), userRepresentation.getId());
        Assert.assertEquals(user.getGivenName(), userRepresentation.getGivenName());
        Assert.assertEquals(user.getSurname(), userRepresentation.getSurname());
        Assert.assertEquals(user.getEmail(), userRepresentation.getEmail());
    }
    
    public static PostDTO samplePost() {
        return new PostDTO()
            .setName("Post")
            .setDescription("desc")
            .setOrganizationName("org")
            .setLocation(new LocationDTO()
                .setName("BB")
                .setDomicile("PL")
                .setGoogleId("sss")
                .setLatitude(BigDecimal.ONE)
                .setLongitude(BigDecimal.ONE))
            .setApplyWebsite("http://www.google.co.uk")
            .setPostCategories(Collections.emptyList())
            .setMemberCategories(Collections.emptyList())
            .setExistingRelation(ExistingRelation.STUDENT)
            .setLiveTimestamp(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
            .setDeadTimestamp(LocalDateTime.now().plusWeeks(1L).truncatedTo(ChronoUnit.SECONDS));
    }
    
    public void verifyResourceOperation(ResourceOperationRepresentation resourceOperationR, Action expectedAction, User expectedUser,
        ResourceChangeListRepresentation expectedChanges) {
        Assert.assertEquals(expectedAction, resourceOperationR.getAction());
        verifyUser(expectedUser, resourceOperationR.getUser());
        Assert.assertEquals(expectedChanges, resourceOperationR.getChangeList());
    }
    
}
