package hr.prism.board.mapper;

import hr.prism.board.domain.Board;
import hr.prism.board.domain.Department;
import hr.prism.board.enums.CategoryType;
import hr.prism.board.representation.BoardRepresentation;
import hr.prism.board.service.ResourceService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.function.Function;

@Service
public class BoardMapper implements Function<Board, BoardRepresentation> {

    @Inject
    private DepartmentMapper departmentMapper;

    @Inject
    private ResourceMapper resourceMapper;

    @Inject
    private ResourceService resourceService;

    @Inject
    private DocumentMapper documentMapper;

    @Override
    public BoardRepresentation apply(Board board) {
        if (board == null) {
            return null;
        }

        Department department = (Department) board.getParent();
        return resourceMapper.apply(board, BoardRepresentation.class)
            .setDocumentLogo(documentMapper.apply(board.getDocumentLogo()))
            .setHandle(resourceMapper.getHandle(board, department))
            .setPostCategories(resourceService.getCategories(board, CategoryType.POST))
            .setDepartment(departmentMapper.apply(department))
            .setPostCount(board.getPostCount())
            .setAuthorCount(board.getAuthorCount());
    }

    public BoardRepresentation applySmall(Board board) {
        if (board == null) {
            return null;
        }

        Department department = (Department) board.getParent();
        return resourceMapper.applySmall(board, BoardRepresentation.class)
            .setDocumentLogo(documentMapper.apply(board.getDocumentLogo()))
            .setHandle(resourceMapper.getHandle(board, department))
            .setDepartment(departmentMapper.applySmall(department));
    }

}
