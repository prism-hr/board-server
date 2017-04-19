package hr.prism.board.service;

import hr.prism.board.TestContext;
import hr.prism.board.domain.Board;
import hr.prism.board.domain.Department;
import hr.prism.board.domain.Post;
import hr.prism.board.dto.LocationDTO;
import hr.prism.board.dto.PostPatchDTO;
import hr.prism.board.exception.ApiException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.exception.ExceptionUtil;
import org.junit.Test;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.Optional;

@TestContext
public class PostServiceIT {
    
    @Inject
    private PostService postService;
    
    private Post post = (Post) new Post().setParent(new Board().setParent(new Department()));
    
    @Test
    public void shouldNotBeAbleToPatchPostWithNullName() {
        ExceptionUtil.verifyApiException(ApiException.class, () ->
                postService.updatePost(post, new PostPatchDTO().setName(Optional.empty())),
            ExceptionCode.MISSING_POST_NAME, null);
    }
    
    @Test
    public void shouldNotBeAbleToPatchPostWithNullDescription() {
        ExceptionUtil.verifyApiException(ApiException.class, () ->
                postService.updatePost(post, new PostPatchDTO().setName(Optional.of("name")).setDescription(Optional.empty())),
            ExceptionCode.MISSING_POST_DESCRIPTION, null);
    }
    
    @Test
    public void shouldNotBeAbleToPatchPostWithNullOrganizationName() {
        ExceptionUtil.verifyApiException(ApiException.class, () ->
                postService.updatePost(post,
                    new PostPatchDTO().setName(Optional.of("name")).setDescription(Optional.of("description")).setOrganizationName(Optional.empty())),
            ExceptionCode.MISSING_POST_ORGANIZATION_NAME, null);
    }
    
    @Test
    public void shouldNotBeAbleToPatchPostWithNullLocation() {
        ExceptionUtil.verifyApiException(ApiException.class, () ->
                postService.updatePost(post,
                    new PostPatchDTO()
                        .setName(Optional.of("name"))
                        .setDescription(Optional.of("description"))
                        .setOrganizationName(Optional.empty())
                        .setLocation(Optional.empty())),
            ExceptionCode.MISSING_POST_LOCATION, null);
    }
    
    @Test
    public void shouldNotBeAbleToPatchPostWithNullApplyWebsiteAndNullApplyEmailAndNullApplyDocument() {
        ExceptionUtil.verifyApiException(ApiException.class, () ->
                postService.updatePost(post,
                    new PostPatchDTO()
                        .setName(Optional.of("name"))
                        .setDescription(Optional.of("description"))
                        .setOrganizationName(Optional.of("organization name"))
                        .setLocation(Optional.of(new LocationDTO()))
                        .setApplyWebsite(Optional.empty())
                        .setApplyEmail(Optional.empty())
                        .setApplyDocument(Optional.empty())),
            ExceptionCode.CORRUPTED_POST_APPLY, null);
    }
    
    @Test
    public void shouldNotBeAbleToPatchPostWithApplyWebsiteAndApplyEmail() {
        ExceptionUtil.verifyApiException(ApiException.class, () ->
                postService.updatePost(post,
                    new PostPatchDTO()
                        .setName(Optional.of("name"))
                        .setDescription(Optional.of("description"))
                        .setOrganizationName(Optional.of("organization name"))
                        .setLocation(Optional.of(new LocationDTO()))
                        .setApplyWebsite(Optional.of("http://www.google.com"))
                        .setApplyEmail(Optional.of("alastair@prism.hr"))),
            ExceptionCode.CORRUPTED_POST_APPLY, null);
    }
    
    @Test
    public void shouldNotBeAbleToPatchPostWithLiveTimestampNull() {
        ExceptionUtil.verifyApiException(ApiException.class, () ->
                postService.updatePost(post,
                    new PostPatchDTO()
                        .setName(Optional.of("name"))
                        .setDescription(Optional.of("description"))
                        .setOrganizationName(Optional.of("organization name"))
                        .setLocation(Optional.of(new LocationDTO()))
                        .setApplyWebsite(Optional.of("http://www.google.com"))
                        .setLiveTimestamp(Optional.empty())),
            ExceptionCode.MISSING_POST_LIVE_TIMESTAMP, null);
    }
    
    @Test
    public void shouldNotBeAbleToPatchPostWithDeadTimestampNull() {
        ExceptionUtil.verifyApiException(ApiException.class, () ->
                postService.updatePost(post,
                    new PostPatchDTO()
                        .setName(Optional.of("name"))
                        .setDescription(Optional.of("description"))
                        .setOrganizationName(Optional.of("organization name"))
                        .setLocation(Optional.of(new LocationDTO()))
                        .setApplyWebsite(Optional.of("http://www.google.com"))
                        .setLiveTimestamp(Optional.of(LocalDateTime.now()))
                        .setDeadTimestamp(Optional.empty())),
            ExceptionCode.MISSING_POST_DEAD_TIMESTAMP, null);
    }
    
}
