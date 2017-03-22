package hr.prism.board.mapper;

import hr.prism.board.domain.Board;
import hr.prism.board.domain.Post;
import hr.prism.board.representation.PostRepresentation;
import hr.prism.board.service.BoardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.function.Function;

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
        return new PostRepresentation()
            .setId(post.getId())
            .setName(post.getName())
            .setDescription(post.getDescription())
            .setOrganizationName(post.getOrganizationName())
            .setLocation(locationMapper.apply(post.getLocation()))
            .setExistingRelation(post.getExistingRelation())
            .setApplyWebsite(post.getApplyWebsite())
            .setApplyDocument(documentMapper.apply(post.getApplyDocument()))
            .setApplyEmail(post.getApplyEmail())
            .setBoard(boardMapper.apply(board));
    }

}
