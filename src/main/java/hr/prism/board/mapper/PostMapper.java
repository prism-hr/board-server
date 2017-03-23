package hr.prism.board.mapper;

import hr.prism.board.domain.Board;
import hr.prism.board.domain.Category;
import hr.prism.board.domain.Post;
import hr.prism.board.enums.CategoryType;
import hr.prism.board.representation.PostRepresentation;
import hr.prism.board.service.BoardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class PostMapper implements Function<Post, PostRepresentation> {

    @Inject
    private LocationMapper locationMapper;

    @Inject
    private DocumentMapper documentMapper;

    @Inject
    private BoardService boardService;

    @Inject
    private BoardMapper boardMapper;

    @Override
    public PostRepresentation apply(Post post) {
        Board board = boardService.findByPost(post);
        List<String> postCategories = post.getPostCategories().stream().filter(c -> c.getType() == CategoryType.POST).map(Category::getName).collect(Collectors.toList());
        List<String> memberCategories = post.getPostCategories().stream().filter(c -> c.getType() == CategoryType.MEMBER).map(Category::getName).collect(Collectors.toList());
        return new PostRepresentation()
            .setId(post.getId())
            .setName(post.getName())
            .setDescription(post.getDescription())
            .setOrganizationName(post.getOrganizationName())
            .setLocation(locationMapper.apply(post.getLocation()))
            .setExistingRelation(post.getExistingRelation())
            .setPostCategories(postCategories)
            .setMemberCategories(memberCategories)
            .setApplyWebsite(post.getApplyWebsite())
            .setApplyDocument(documentMapper.apply(post.getApplyDocument()))
            .setApplyEmail(post.getApplyEmail())
            .setBoard(boardMapper.apply(board));
    }

}
