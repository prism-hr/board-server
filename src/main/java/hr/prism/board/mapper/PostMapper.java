package hr.prism.board.mapper;

import hr.prism.board.domain.*;
import hr.prism.board.enums.CategoryType;
import hr.prism.board.representation.PostRepresentation;
import hr.prism.board.service.BoardService;
import hr.prism.board.service.UserService;
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
    private BoardService boardService;

    @Inject
    private BoardMapper boardMapper;

    @Inject
    private UserRoleService userRoleService;

    @Inject
    private ActionService actionService;

    @Inject
    private UserService userService;

    public Function<Post, PostRepresentation> create() {
        return create(new HashSet<>());
    }

    // TODO: refactor, this is getting too complicated
    public Function<Post, PostRepresentation> create(Set<String> options) {
        User user = userService.getCurrentUser();
        return (Post post) -> {
            Board board = boardService.findByPost(post);
            List<String> postCategories = post.getPostCategories().stream().filter(c -> c.getType() == CategoryType.POST).map(ResourceCategory::getName).collect(Collectors
                .toList());
            List<String> memberCategories = post.getPostCategories().stream().filter(c -> c.getType() == CategoryType.MEMBER).map(ResourceCategory::getName).collect(Collectors
                .toList());

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

            if (options.contains("roles")) {
                postRepresentation.setRoles(userRoleService.findByResourceAndUser(board, user));
                postRepresentation.setActions(actionService.getActions(post, user));
            }

            return postRepresentation;
        };
    }

}
