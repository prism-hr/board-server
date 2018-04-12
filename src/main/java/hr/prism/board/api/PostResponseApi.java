package hr.prism.board.api;

import hr.prism.board.dto.ResourceEventDTO;
import hr.prism.board.mapper.ResourceEventMapper;
import hr.prism.board.representation.ResourceEventRepresentation;
import hr.prism.board.service.PostService;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
public class PostResponseApi {

    private final PostService postService;

    private final ResourceEventMapper resourceEventMapper;

    @Inject
    public PostResponseApi(PostService postService, ResourceEventMapper resourceEventMapper) {
        this.postService = postService;
        this.resourceEventMapper = resourceEventMapper;
    }

    @RequestMapping(value = "/api/posts/referrals/{referral}", method = GET)
    public void getPostReferral(@PathVariable String referral, HttpServletResponse response) throws IOException {
        response.sendRedirect(postService.getPostReferral(referral));
    }

    @RequestMapping(value = "/api/posts/{postId}/respond", method = POST)
    public ResourceEventRepresentation createPostResponse(@PathVariable Long postId,
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
    public ResourceEventRepresentation viewPostResponse(@PathVariable Long postId, @PathVariable Long responseId) {
        return resourceEventMapper.apply(postService.putPostResponseView(postId, responseId));
    }

}
