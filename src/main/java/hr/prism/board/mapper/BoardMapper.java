package hr.prism.board.mapper;

import com.google.common.base.Splitter;
import hr.prism.board.domain.Board;
import hr.prism.board.representation.BoardRepresentation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.function.Function;

@Service
@Transactional
public class BoardMapper implements Function<Board, BoardRepresentation> {

    @Inject
    private DepartmentMapper departmentMapper;

    @Override
    public BoardRepresentation apply(Board board) {

        return new BoardRepresentation()
                .withId(board.getId())
                .withName(board.getName())
                .withPurpose(board.getPurpose())
                .withPostCategories(Splitter.on("|").omitEmptyStrings().splitToList(board.getPostCategories()))
                .withDepartment(departmentMapper.apply(board.getDepartment()));
    }
}
