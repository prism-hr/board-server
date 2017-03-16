package hr.prism.board.mapper;

import com.google.common.base.Splitter;
import hr.prism.board.domain.Board;
import hr.prism.board.representation.BoardRepresentation;
import hr.prism.board.service.DepartmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.function.Function;

@Service
@Transactional
public class BoardMapper implements Function<Board, BoardRepresentation> {
    
    @Inject
    private DepartmentMapperFactory departmentMapperFactory;
    
    @Inject
    private DepartmentService departmentService;
    
    @Override
    public BoardRepresentation apply(Board board) {
        return new BoardRepresentation()
            .setId(board.getId())
            .setName(board.getName())
            .setPurpose(board.getDescription())
            .setHandle(board.getHandle())
            .setPostCategories(Splitter.on("|").omitEmptyStrings().splitToList(board.getCategoryList()))
            .setDepartment(departmentMapperFactory.create().apply(departmentService.findByBoard(board)))
            .setDefaultPostVisibility(board.getDefaultPostVisibility());
    }
    
}
