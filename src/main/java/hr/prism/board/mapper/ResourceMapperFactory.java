package hr.prism.board.mapper;

import hr.prism.board.domain.Board;
import hr.prism.board.domain.Department;
import hr.prism.board.domain.Post;
import hr.prism.board.domain.Resource;
import hr.prism.board.representation.ResourceRepresentation;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class ResourceMapperFactory {

    private final DepartmentMapper departmentMapper;

    private final BoardMapper boardMapper;

    private final PostMapper postMapper;

    @Inject
    public ResourceMapperFactory(DepartmentMapper departmentMapper, BoardMapper boardMapper, PostMapper postMapper) {
        this.departmentMapper = departmentMapper;
        this.boardMapper = boardMapper;
        this.postMapper = postMapper;
    }

    ResourceRepresentation applySmall(Resource resource) {
        if (resource instanceof Department) {
            return departmentMapper.applySmall((Department) resource);
        } else if (resource instanceof Board) {
            return boardMapper.applySmall((Board) resource);
        }

        return postMapper.applySmall((Post) resource);
    }

}
