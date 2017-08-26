package hr.prism.board.api;

import hr.prism.board.domain.Post;
import hr.prism.board.dto.PostDTO;
import hr.prism.board.dto.PostPatchDTO;
import hr.prism.board.dto.ResourceEventDTO;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.Scope;
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

    @RequestMapping(value = "/api/boards/{id}/posts", method = RequestMethod.POST)
    public PostRepresentation postPost(@PathVariable Long id, @RequestBody @Valid PostDTO postDTO) {
        Post post = postService.createPost(id, postDTO);
        return postMapper.apply(post);
    }

    @RequestMapping(value = "/api/posts", method = RequestMethod.GET)
    public List<PostRepresentation> getPosts(@RequestParam(required = false) Boolean includePublicPosts) {
        return postService.getPosts(null, includePublicPosts).stream().map(post -> postMapper.apply(post)).collect(Collectors.toList());
    }

    @RequestMapping(value = "/api/boards/{boardId}/posts", method = RequestMethod.GET)
    public List<PostRepresentation> getPostsByBoard(@PathVariable Long boardId, @RequestParam(required = false) Boolean includePublicPosts) {
        return postService.getPosts(boardId, includePublicPosts).stream().map(post -> postMapper.apply(post)).collect(Collectors.toList());
    }

    @RequestMapping(value = "/api/posts/{id}", method = RequestMethod.GET)
    public PostRepresentation getPost(@PathVariable Long id, HttpServletRequest request) {
        return postMapper.apply(postService.getPost(id, BoardUtils.getClientIpAddress(request), true));
    }

    @RequestMapping(value = "/api/posts/{id}/operations", method = RequestMethod.GET)
    public List<ResourceOperationRepresentation> getPostOperations(@PathVariable Long id) {
        return resourceService.getResourceOperations(Scope.POST, id).stream()
            .map(resourceOperation -> resourceOperationMapper.apply(resourceOperation)).collect(Collectors.toList());
    }

    @RequestMapping(value = "/api/posts/{id}", method = RequestMethod.PATCH)
    public PostRepresentation patchPost(@PathVariable Long id, @RequestBody @Valid PostPatchDTO postDTO) {
        return postMapper.apply(postService.executeAction(id, Action.EDIT, postDTO));
    }

    @RequestMapping(value = "/api/posts/{id}/actions/{action}", method = RequestMethod.POST)
    public PostRepresentation executeAction(@PathVariable Long id, @PathVariable String action, @RequestBody @Valid PostPatchDTO postDTO) {
        return postMapper.apply(postService.executeAction(id, Action.exchangeAndValidate(action, postDTO), postDTO));
    }

    @RequestMapping(value = "/api/posts/organizations", method = RequestMethod.GET)
    public List<String> lookupOrganizations(@RequestParam String query) {
        return postService.findOrganizationsBySimilarName(query);
    }

    @RequestMapping(value = "api/posts/{referral}", method = RequestMethod.GET)
    public void getPostReferral(@PathVariable String referral, HttpServletResponse response) throws IOException {
        response.sendRedirect(postService.getPostReferral(referral));
    }

    @RequestMapping(value = "api/posts/{postId}/respond", method = RequestMethod.POST)
    public ResourceEventRepresentation postPostResponse(@PathVariable Long postId, @RequestBody @Valid ResourceEventDTO resourceEvent) {
        return resourceEventMapper.apply(postService.postPostResponse(postId, resourceEvent));
    }

    @RequestMapping(value = "api/posts/{postId}/responses", method = RequestMethod.GET)
    public List<ResourceEventRepresentation> getPostResponses(@PathVariable Long postId) {
        return postService.getPostResponses(postId).stream().map(resourceEventMapper).collect(Collectors.toList());
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
