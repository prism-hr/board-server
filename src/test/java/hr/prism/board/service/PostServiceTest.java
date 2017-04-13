package hr.prism.board.service;

import hr.prism.board.domain.Board;
import hr.prism.board.domain.Department;
import hr.prism.board.domain.Post;
import hr.prism.board.dto.LocationDTO;
import hr.prism.board.dto.PostPatchDTO;
import hr.prism.board.exception.ExceptionUtil;
import org.junit.Test;

import java.util.Optional;

public class PostServiceTest {
    
    private PostService postService = new PostService();
    
    private Post post = (Post) new Post().setParent(new Board().setParent(new Department()));
    
    @Test
    public void shouldNotBeAbleToPatchPostWithNullName() {
        ExceptionUtil.verifyIllegalStateException(() ->
                postService.updatePost(post, new PostPatchDTO().setName(Optional.empty())),
            "Attempted to set post name to null");
    }
    
    @Test
    public void shouldNotBeAbleToPatchPostWithNullDescription() {
        ExceptionUtil.verifyIllegalStateException(() ->
                postService.updatePost(post, new PostPatchDTO().setName(Optional.of("name")).setDescription(Optional.empty())),
            "Attempted to set post description to null");
    }
    
    @Test
    public void shouldNotBeAbleToPatchPostWithNullOrganizationName() {
        ExceptionUtil.verifyIllegalStateException(() ->
                postService.updatePost(post,
                    new PostPatchDTO().setName(Optional.of("name")).setDescription(Optional.of("description")).setOrganizationName(Optional.empty())),
            "Attempted to set post organization name to null");
    }
    
    @Test
    public void shouldNotBeAbleToPatchPostWithNullLocation() {
        ExceptionUtil.verifyIllegalStateException(() ->
                postService.updatePost(post,
                    new PostPatchDTO()
                        .setName(Optional.of("name"))
                        .setDescription(Optional.of("description"))
                        .setOrganizationName(Optional.empty())
                        .setLocation(Optional.empty())),
            "Attempted to set post location to null");
    }
    
    @Test
    public void shouldNotBeAbleToPatchPostWithNullApplyWebsiteAndNullApplyEmailAndNullApplyDocument() {
        ExceptionUtil.verifyIllegalStateException(() ->
                postService.updatePost(post,
                    new PostPatchDTO()
                        .setName(Optional.of("name"))
                        .setDescription(Optional.of("description"))
                        .setOrganizationName(Optional.of("organization name"))
                        .setLocation(Optional.of(new LocationDTO()))
                        .setApplyWebsite(Optional.empty())
                        .setApplyEmail(Optional.empty())
                        .setApplyDocument(Optional.empty())),
            "Attempted to set post application mechanism to null");
    }
    
}
