package hr.prism.board.api;

import hr.prism.board.dto.ResourceEventDTO;
import hr.prism.board.mapper.ResourceEventMapper;
import hr.prism.board.representation.ResourceEventRepresentation;
import hr.prism.board.service.PostResponseService;
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

    private final PostResponseService postResponseService;

    private final ResourceEventMapper resourceEventMapper;

    @Inject
    public PostResponseApi(PostResponseService postResponseService, ResourceEventMapper resourceEventMapper) {
        this.postResponseService = postResponseService;
        this.resourceEventMapper = resourceEventMapper;
    }

    @RequestMapping(value = "/api/posts/referrals/{referral}", method = GET)
    public void consumePostReferral(@PathVariable String referral, HttpServletResponse response) throws IOException {
        response.sendRedirect(postResponseService.consumePostReferral(referral));
    }

    @RequestMapping(value = "/api/posts/{postId}/respond", method = POST)
    public ResourceEventRepresentation createPostResponse(@PathVariable Long postId,
                                                          @RequestBody @Valid ResourceEventDTO resourceEvent) {
        return resourceEventMapper.apply(postResponseService.createPostResponse(postId, resourceEvent));
    }

    @RequestMapping(value = "/api/posts/{postId}/responses", method = GET)
    public List<ResourceEventRepresentation> getPostResponses(@PathVariable Long postId,
                                                              @RequestParam(required = false) String searchTerm) {
        return postResponseService.getPostResponses(postId, searchTerm)
            .stream().map(resourceEventMapper).collect(toList());
    }

    @RequestMapping(value = "/api/posts/{postId}/responses/{responseId}", method = GET)
    public ResourceEventRepresentation getPostResponse(@PathVariable Long postId, @PathVariable Long responseId) {
        return resourceEventMapper.apply(postResponseService.getPostResponse(postId, responseId));
    }

    @RequestMapping(value = "/api/posts/{postId}/responses/{responseId}/view", method = PUT)
    public ResourceEventRepresentation viewPostResponse(@PathVariable Long postId, @PathVariable Long responseId) {
        return resourceEventMapper.apply(postResponseService.createPostResponseView(postId, responseId));
    }

}
