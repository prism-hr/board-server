package hr.prism.board.mapper;

import hr.prism.board.domain.ActionService;
import hr.prism.board.domain.Board;
import hr.prism.board.domain.Post;
import hr.prism.board.domain.ResourceCategory;
import hr.prism.board.enums.CategoryType;
import hr.prism.board.representation.PostRepresentation;
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
public class PostMapper {
    
    @Inject
    private LocationMapper locationMapper;
    
    @Inject
    private DocumentMapper documentMapper;
    
    @Inject
    private BoardMapper boardMapper;
    
    @Inject
    private ActionService actionService;
    
    public Function<Post, PostRepresentation> create() {
        return create(new HashSet<>());
    }
    
    public Function<Post, PostRepresentation> create(Set<String> options) {
        return (Post post) -> {
            Board board = (Board) post.getParent();
            List<String> postCategories = post.getCategories().stream().filter(category -> category.getType() == CategoryType.POST)
                .map(ResourceCategory::getName).collect(Collectors.toList());
            List<String> memberCategories = post.getCategories().stream().filter(category -> category.getType() == CategoryType.MEMBER)
                .map(ResourceCategory::getName).collect(Collectors.toList());
            
            PostRepresentation postRepresentation = new PostRepresentation();
            postRepresentation
                .setId(post.getId())
                .setName(post.getName())
                .setState(post.getState());
            postRepresentation
                .setDescription(post.getDescription())
                .setOrganizationName(post.getOrganizationName())
                .setLocation(locationMapper.apply(post.getLocation()))
                .setExistingRelation(post.getExistingRelation())
                .setPostCategories(postCategories)
                .setMemberCategories(memberCategories)
                .setApplyWebsite(post.getApplyWebsite())
                .setApplyDocument(documentMapper.apply(post.getApplyDocument()))
                .setApplyEmail(post.getApplyEmail())
                .setBoard(boardMapper.create().apply(board));
    
            if (options.contains("actions")) {
                postRepresentation.setActions(actionService.getActions(post));
            }
    
            return postRepresentation;
        };
    }
    
}
