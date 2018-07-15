package hr.prism.board.api;

import hr.prism.board.domain.User;
import hr.prism.board.dto.ResourceEventDTO;
import hr.prism.board.mapper.ResourceEventMapper;
import hr.prism.board.representation.ResourceEventRepresentation;
import hr.prism.board.service.ResourceEventService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
public class PostEventApi {

    private final ResourceEventService resourceEventService;

    private final ResourceEventMapper resourceEventMapper;

    @Inject
    public PostEventApi(ResourceEventService resourceEventService, ResourceEventMapper resourceEventMapper) {
        this.resourceEventService = resourceEventService;
        this.resourceEventMapper = resourceEventMapper;
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/api/posts/{postId}/referrals/{referral}", method = GET)
    public void consumePostReferral(@AuthenticationPrincipal User user, @PathVariable Long postId,
                                    @PathVariable String referral, HttpServletResponse response) throws IOException {
        response.sendRedirect(resourceEventService.processReferral(postId, user, referral));
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/api/posts/{postId}/respond", method = POST)
    public ResourceEventRepresentation createPostResponse(@AuthenticationPrincipal User user, @PathVariable Long postId,
                                                          @RequestBody @Valid ResourceEventDTO resourceEvent) {
        return resourceEventMapper.apply(resourceEventService.processResponse(postId, user, resourceEvent));
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/api/posts/{postId}/responses", method = GET)
    public List<ResourceEventRepresentation> getPostResponses(@AuthenticationPrincipal User user,
                                                              @PathVariable Long postId,
                                                              @RequestParam(required = false) String searchTerm) {
        return resourceEventService.getResponses(user, postId, searchTerm)
            .stream().map(resourceEventMapper).collect(toList());
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/api/posts/{postId}/responses/{responseId}", method = GET)
    public ResourceEventRepresentation getPostResponse(@AuthenticationPrincipal User user,
                                                       @PathVariable Long postId, @PathVariable Long responseId) {
        return resourceEventMapper.apply(resourceEventService.getResponse(user, postId, responseId));
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/api/posts/{postId}/responses/{responseId}/view", method = PUT)
    public ResourceEventRepresentation viewPostResponse(@AuthenticationPrincipal User user, @PathVariable Long postId,
                                                        @PathVariable Long responseId) {
        return resourceEventMapper.apply(resourceEventService.createResponseView(user, postId, responseId));
    }

}
