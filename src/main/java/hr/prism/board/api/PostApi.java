package hr.prism.board.api;

import hr.prism.board.domain.Post;
import hr.prism.board.dto.PostDTO;
import hr.prism.board.dto.PostPatchDTO;
import hr.prism.board.dto.ResourceEventDTO;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.State;
import hr.prism.board.mapper.OrganizationMapper;
import hr.prism.board.mapper.PostMapper;
import hr.prism.board.mapper.ResourceEventMapper;
import hr.prism.board.mapper.ResourceOperationMapper;
import hr.prism.board.representation.OrganizationRepresentation;
import hr.prism.board.representation.PostRepresentation;
import hr.prism.board.representation.ResourceEventRepresentation;
import hr.prism.board.representation.ResourceOperationRepresentation;
import hr.prism.board.service.PostService;
import hr.prism.board.service.ResourceService;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

import static hr.prism.board.enums.Scope.POST;
import static hr.prism.board.utils.BoardUtils.getClientIpAddress;
import static java.util.stream.Collectors.toList;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
public class PostApi {

    private final PostService postService;

    private final PostMapper postMapper;

    private final OrganizationMapper organizationMapper;

    private final ResourceService resourceService;

    private final ResourceOperationMapper resourceOperationMapper;

    private final ResourceEventMapper resourceEventMapper;

    @Inject
    public PostApi(PostService postService, PostMapper postMapper, OrganizationMapper organizationMapper,
                   ResourceService resourceService, ResourceOperationMapper resourceOperationMapper,
                   ResourceEventMapper resourceEventMapper) {
        this.postService = postService;
        this.postMapper = postMapper;
        this.organizationMapper = organizationMapper;
        this.resourceService = resourceService;
        this.resourceOperationMapper = resourceOperationMapper;
        this.resourceEventMapper = resourceEventMapper;
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
        return resourceService.getResourceOperations(POST, postId)
            .stream().map(resourceOperationMapper).collect(toList());
    }

    @RequestMapping(value = "/api/posts/{postId}", method = PATCH)
    public PostRepresentation patchPost(@PathVariable Long postId, @RequestBody @Valid PostPatchDTO postDTO) {
        return postMapper.apply(postService.executeAction(postId, Action.EDIT, postDTO));
    }

    @RequestMapping(value = "/api/posts/{postId}/actions/{action}", method = RequestMethod.POST)
    public PostRepresentation executeAction(@PathVariable Long postId, @PathVariable String action,
                                            @RequestBody @Valid PostPatchDTO postDTO) {
        return postMapper.apply(postService.executeAction(postId, Action.valueOf(action.toUpperCase()), postDTO));
    }

    @RequestMapping(value = "/api/posts/organizations", method = GET)
    public List<OrganizationRepresentation> lookupOrganizations(@RequestParam String query) {
        return postService.findOrganizationsBySimilarName(query)
            .stream().map(organizationMapper::applySmall).collect(toList());
    }

    @RequestMapping(value = "/api/posts/referrals/{referral}", method = GET)
    public void getPostReferral(@PathVariable String referral, HttpServletResponse response) throws IOException {
        response.sendRedirect(postService.getPostReferral(referral));
    }

    @RequestMapping(value = "/api/posts/{postId}/respond", method = RequestMethod.POST)
    public ResourceEventRepresentation postPostResponse(@PathVariable Long postId,
                                                        @RequestBody @Valid ResourceEventDTO resourceEvent) {
        return resourceEventMapper.apply(postService.createPostResponse(postId, resourceEvent));
    }

    @RequestMapping(value = "/api/posts/{postId}/responses", method = GET)
    public List<ResourceEventRepresentation> getPostResponses(@PathVariable Long postId,
                                                              @RequestParam(required = false) String searchTerm) {
        return postService.getPostResponses(postId, searchTerm).stream().map(resourceEventMapper).collect(toList());
    }

    @RequestMapping(value = "/api/posts/{postId}/responses/{responseId}", method = GET)
    public ResourceEventRepresentation getPostResponse(@PathVariable Long postId, @PathVariable Long responseId) {
        return resourceEventMapper.apply(postService.getPostResponse(postId, responseId));
    }

    @RequestMapping(value = "/api/posts/{postId}/responses/{responseId}/view", method = PUT)
    public ResourceEventRepresentation putPostResponseView(@PathVariable Long postId, @PathVariable Long responseId) {
        return resourceEventMapper.apply(postService.putPostResponseView(postId, responseId));
    }

    @RequestMapping(value = "/api/posts/archiveQuarters", method = GET)
    public List<String> getPostArchiveQuarters(@RequestParam(required = false) Long parentId) {
        return resourceService.getResourceArchiveQuarters(POST, parentId);
    }

}
