package hr.prism.board.mapper;

import com.google.common.base.Splitter;
import hr.prism.board.domain.Department;
import hr.prism.board.representation.BoardRepresentation;
import hr.prism.board.representation.DepartmentRepresentation;
import hr.prism.board.service.BoardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class DepartmentMapperFactory {
    
    @Inject
    private DocumentMapper documentMapper;
    
    @Inject
    private BoardMapper boardMapper;
    
    @Inject
    private BoardService boardService;
    
    public Function<Department, DepartmentRepresentation> create() {
        return create(new HashSet<>());
    }
    
    public Function<Department, DepartmentRepresentation> create(Set<String> options) {
        return (Department department) -> {
            DepartmentRepresentation departmentRepresentation = new DepartmentRepresentation()
                .setId(department.getId())
                .setName(department.getName())
                .setDocumentLogo(documentMapper.apply(department.getDocumentLogo()))
                .setHandle(department.getHandle())
                .setMemberCategories(Splitter.on("|").omitEmptyStrings().splitToList(department.getCategoryList()));
            if (options.contains("boards")) {
                List<BoardRepresentation> boards = boardService.findByDepartment(department)
                    .stream()
                    .map(boardMapper)
                    .collect(Collectors.toList());
                departmentRepresentation.setBoards(boards);
            }
            return departmentRepresentation;
        };
    }
}
