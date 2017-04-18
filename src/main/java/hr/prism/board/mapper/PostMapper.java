package hr.prism.board.mapper;

import hr.prism.board.domain.Board;
import hr.prism.board.domain.Post;
import hr.prism.board.enums.CategoryType;
import hr.prism.board.representation.PostRepresentation;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Service
public class PostMapper implements Function<Post, PostRepresentation> {
    
    @Inject
    private LocationMapper locationMapper;
    
    @Inject
    private DocumentMapper documentMapper;
    
    @Inject
    private BoardMapper boardMapper;
    
    @Override
    public PostRepresentation apply(Post post) {
        if (post == null) {
            return null;
        }
        
        Board board = (Board) post.getParent();
        List<String> postCategories = new ArrayList<>();
        List<String> memberCategories = new ArrayList<>();
        post.getCategories().forEach(category -> {
            if (category.getType() == CategoryType.POST) {
                postCategories.add(category.getName());
            } else {
                memberCategories.add(category.getName());
            }
        });
        
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
            .setExistingRelationExplanation(post.getExistingRelationExplanation())
            .setPostCategories(postCategories)
            .setMemberCategories(memberCategories)
            .setApplyWebsite(post.getApplyWebsite())
            .setApplyDocument(documentMapper.apply(post.getApplyDocument()))
            .setApplyEmail(post.getApplyEmail())
            .setBoard(boardMapper.apply(board))
            .setLiveTimestamp(post.getLiveTimestamp())
            .setDeadTimestamp(post.getDeadTimestamp());
        
        postRepresentation.setActions(post.getActions());
        return postRepresentation;
    }
    
}
