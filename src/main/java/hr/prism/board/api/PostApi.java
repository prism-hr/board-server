package hr.prism.board.api;

import hr.prism.board.domain.Post;
import hr.prism.board.dto.PostDTO;
import hr.prism.board.dto.PostPatchDTO;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.State;
import hr.prism.board.mapper.OrganizationMapper;
import hr.prism.board.mapper.PostMapper;
import hr.prism.board.mapper.ResourceOperationMapper;
import hr.prism.board.representation.OrganizationRepresentation;
import hr.prism.board.representation.PostRepresentation;
import hr.prism.board.representation.ResourceOperationRepresentation;
import hr.prism.board.service.OrganizationService;
import hr.prism.board.service.PostService;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

import static hr.prism.board.utils.BoardUtils.getClientIpAddress;
import static java.util.stream.Collectors.toList;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.PATCH;

@RestController
public class PostApi {

    private final PostService postService;

    private final OrganizationService organizationService;

    private final PostMapper postMapper;

    private final OrganizationMapper organizationMapper;

    private final ResourceOperationMapper resourceOperationMapper;

    @Inject
    public PostApi(PostService postService, OrganizationService organizationService, PostMapper postMapper,
                   OrganizationMapper organizationMapper, ResourceOperationMapper resourceOperationMapper) {
        this.postService = postService;
        this.organizationService = organizationService;
        this.postMapper = postMapper;
        this.organizationMapper = organizationMapper;
        this.resourceOperationMapper = resourceOperationMapper;
    }

    @RequestMapping(value = "/api/boards/{boardId}/posts", method = RequestMethod.POST)
    public PostRepresentation postPost(@PathVariable Long boardId, @RequestBody @Valid PostDTO postDTO) {
        Post post = postService.createPost(boardId, postDTO);
        return postMapper.apply(post);
    }

    @RequestMapping(value = "/api/posts", method = GET)
    public List<PostRepresentation> getPosts(@RequestParam(required = false) Long parentId,
                                             @RequestParam(required = false) Boolean includePublic,
                                             @RequestParam(required = false) State state,
                                             @RequestParam(required = false) String quarter,
                                             @RequestParam(required = false) String searchTerm) {
        return postService.getPosts(parentId, includePublic, state, quarter, searchTerm)
            .stream().map(postMapper).collect(toList());
    }

    @RequestMapping(value = "/api/posts/{postId}", method = GET)
    public PostRepresentation getPost(@PathVariable Long postId, HttpServletRequest request) {
        return postMapper.apply(postService.getPost(postId, getClientIpAddress(request), true));
    }

    @RequestMapping(value = "/api/posts/{postId}/operations", method = GET)
    public List<ResourceOperationRepresentation> getPostOperations(@PathVariable Long postId) {
        return postService.getPostOperations(postId).stream().map(resourceOperationMapper).collect(toList());
    }

    @RequestMapping(value = "/api/posts/{postId}", method = PATCH)
    public PostRepresentation updatePost(@PathVariable Long postId, @RequestBody @Valid PostPatchDTO postDTO) {
        return postMapper.apply(postService.executeAction(postId, Action.EDIT, postDTO));
    }

    @RequestMapping(value = "/api/posts/{postId}/actions/{action}", method = RequestMethod.POST)
    public PostRepresentation executeActionOnPost(@PathVariable Long postId, @PathVariable String action,
                                                  @RequestBody @Valid PostPatchDTO postDTO) {
        return postMapper.apply(postService.executeAction(postId, Action.valueOf(action.toUpperCase()), postDTO));
    }

    @RequestMapping(value = "/api/posts/organizations", method = GET)
    public List<OrganizationRepresentation> findPostOrganizations(@RequestParam String query) {
        return organizationService.findOrganizations(query).stream().map(organizationMapper::apply).collect(toList());
    }

    @RequestMapping(value = "/api/posts/archiveQuarters", method = GET)
    public List<String> getPostArchiveQuarters(@RequestParam(required = false) Long parentId) {
        return postService.getPostArchiveQuarters(parentId);
    }

}
