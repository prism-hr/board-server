package hr.prism.board.mapper;

import hr.prism.board.domain.Board;
import hr.prism.board.domain.Department;
import hr.prism.board.domain.ResourceCategory;
import hr.prism.board.representation.BoardRepresentation;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class BoardMapper implements Function<Board, BoardRepresentation> {

    @Inject
    private DepartmentMapper departmentMapper;

    @Override
    public BoardRepresentation apply(Board board) {
        if (board == null) {
            return null;
        }

        Department department = (Department) board.getParent();
        BoardRepresentation boardRepresentation = new BoardRepresentation();
        boardRepresentation
            .setId(board.getId())
            .setScope(board.getScope())
            .setName(board.getName())
            .setState(board.getState());
        boardRepresentation
            .setPurpose(board.getDescription())
            .setHandle(board.getHandle().replaceFirst(department.getHandle() + "/", ""))
            .setPostCategories(board.getCategories().stream()
                .filter(resourceCategory -> BooleanUtils.isTrue(resourceCategory.getActive())).map(ResourceCategory::getName).collect(Collectors.toList()))
            .setDepartment(departmentMapper.apply(department))
            .setDefaultPostVisibility(board.getDefaultPostVisibility());

        boardRepresentation.setActions(board.getActions());
        return boardRepresentation;
    }

}
