package hr.prism.board.api;

import hr.prism.board.domain.Post;
import hr.prism.board.dto.PostDTO;
import hr.prism.board.dto.PostPatchDTO;
import hr.prism.board.dto.ResourceEventDTO;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.Scope;
import hr.prism.board.enums.State;
import hr.prism.board.mapper.PostMapper;
import hr.prism.board.mapper.ResourceEventMapper;
import hr.prism.board.mapper.ResourceOperationMapper;
import hr.prism.board.representation.PostRepresentation;
import hr.prism.board.representation.ResourceEventRepresentation;
import hr.prism.board.representation.ResourceOperationRepresentation;
import hr.prism.board.service.PostService;
import hr.prism.board.service.ResourceService;
import hr.prism.board.util.BoardUtils;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@SuppressWarnings("SpringAutowiredFieldsWarningInspection")
public class PostApi {

    @Inject
    private PostService postService;

    @Inject
    private PostMapper postMapper;

    @Inject
    private ResourceService resourceService;

    @Inject
    private ResourceOperationMapper resourceOperationMapper;

    @Inject
    private ResourceEventMapper resourceEventMapper;

    @RequestMapping(value = "/api/boards/{boardId}/posts", method = RequestMethod.POST)
    public PostRepresentation postPost(@PathVariable Long boardId, @RequestBody @Valid PostDTO postDTO) {
        Post post = postService.createPost(boardId, postDTO);
        return postMapper.apply(post);
    }

    @RequestMapping(value = "/api/posts", method = RequestMethod.GET)
    public List<PostRepresentation> getPosts(@RequestParam(required = false) Long parentId, @RequestParam(required = false) Boolean includePublic,
                                             @RequestParam(required = false) State state, @RequestParam(required = false) String quarter,
                                             @RequestParam(required = false) String searchTerm) {
        return postService.getPosts(parentId, includePublic, state, quarter, searchTerm).stream().map(postMapper).collect(Collectors.toList());
    }

    @RequestMapping(value = "/api/posts/{postId}", method = RequestMethod.GET)
    public PostRepresentation getPost(@PathVariable Long postId, HttpServletRequest request) {
        return postMapper.apply(postService.getPost(postId, BoardUtils.getClientIpAddress(request), true));
    }

    @RequestMapping(value = "/api/posts/{postId}/operations", method = RequestMethod.GET)
    public List<ResourceOperationRepresentation> getPostOperations(@PathVariable Long postId) {
        return resourceService.getResourceOperations(Scope.POST, postId).stream()
            .map(resourceOperation -> resourceOperationMapper.apply(resourceOperation)).collect(Collectors.toList());
    }

    @RequestMapping(value = "/api/posts/{postId}", method = RequestMethod.PATCH)
    public PostRepresentation patchPost(@PathVariable Long postId, @RequestBody @Valid PostPatchDTO postDTO) {
        return postMapper.apply(postService.executeAction(postId, Action.EDIT, postDTO));
    }

    @RequestMapping(value = "/api/posts/{postId}/actions/{action}", method = RequestMethod.POST)
    public PostRepresentation executeAction(@PathVariable Long postId, @PathVariable String action, @RequestBody @Valid PostPatchDTO postDTO) {
        return postMapper.apply(postService.executeAction(postId, Action.exchangeAndValidate(action, postDTO), postDTO));
    }

    @RequestMapping(value = "/api/posts/organizations", method = RequestMethod.GET)
    public List<String> lookupOrganizations(@RequestParam String query) {
        return postService.findOrganizationsBySimilarName(query);
    }

    @RequestMapping(value = "api/posts/referrals/{referral}", method = RequestMethod.GET)
    public void getPostReferral(@PathVariable String referral, HttpServletResponse response) throws IOException {
        response.sendRedirect(postService.getPostReferral(referral));
    }

    @RequestMapping(value = "api/posts/{postId}/respond", method = RequestMethod.POST)
    public ResourceEventRepresentation postPostResponse(@PathVariable Long postId, @RequestBody @Valid ResourceEventDTO resourceEvent) {
        return resourceEventMapper.apply(postService.createPostResponse(postId, resourceEvent));
    }

    @RequestMapping(value = "api/posts/{postId}/responses", method = RequestMethod.GET)
    public List<ResourceEventRepresentation> getPostResponses(@PathVariable Long postId, @RequestParam(required = false) String searchTerm) {
        return postService.getPostResponses(postId, searchTerm).stream().map(resourceEventMapper).collect(Collectors.toList());
    }

    @RequestMapping(value = "api/posts/{postId}/responses/{responseId}", method = RequestMethod.GET)
    public ResourceEventRepresentation getPostResponse(@PathVariable Long postId, @PathVariable Long responseId) {
        return resourceEventMapper.apply(postService.getPostResponse(postId, responseId));
    }

    @RequestMapping(value = "api/posts/{postId}/responses/{responseId}/view", method = RequestMethod.PUT)
    public ResourceEventRepresentation putPostResponseView(@PathVariable Long postId, @PathVariable Long responseId) {
        return resourceEventMapper.apply(postService.putPostResponseView(postId, responseId));
    }

}
