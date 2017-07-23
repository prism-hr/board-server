package hr.prism.board.mapper;

import hr.prism.board.domain.Board;
import hr.prism.board.domain.Department;
import hr.prism.board.domain.Post;
import hr.prism.board.domain.Resource;
import hr.prism.board.representation.ResourceRepresentation;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class ResourceMapperFactory {

    @Inject
    private DepartmentMapper departmentMapper;

    @Inject
    private BoardMapper boardMapper;

    @Inject
    private PostMapper postMapper;

    public ResourceRepresentation applySmall(Resource resource) {
        if (resource instanceof Department) {
            return departmentMapper.applySmall((Department) resource);
        } else if (resource instanceof Board) {
            return boardMapper.applySmall((Board) resource);
        }

        return postMapper.applySmall((Post) resource);
    }

}
