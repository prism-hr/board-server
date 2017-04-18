package hr.prism.board.api;

import com.google.common.collect.ImmutableMap;
import hr.prism.board.domain.Post;
import hr.prism.board.dto.PostDTO;
import hr.prism.board.dto.PostPatchDTO;
import hr.prism.board.enums.Action;
import hr.prism.board.exception.ApiException;
import hr.prism.board.mapper.PostMapper;
import hr.prism.board.mapper.ResourceOperationMapper;
import hr.prism.board.representation.PostRepresentation;
import hr.prism.board.representation.ResourceOperationRepresentation;
import hr.prism.board.service.PostService;
import hr.prism.board.service.ResourceService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;
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
    
    @RequestMapping(value = "/boards/{id}/posts", method = RequestMethod.POST)
    public PostRepresentation postPost(@PathVariable Long id, @RequestBody @Valid PostDTO postDTO) {
        Post post = postService.createPost(id, postDTO);
        return postMapper.apply(post);
    }
    
    @RequestMapping(value = "posts", method = RequestMethod.GET)
    public List<PostRepresentation> getPosts() {
        return postService.getPosts(null).stream().map(post -> postMapper.apply(post)).collect(Collectors.toList());
    }
    
    @RequestMapping(value = "/boards/{boardId}/posts", method = RequestMethod.GET)
    public List<PostRepresentation> getPostsByBoard(@PathVariable Long boardId) {
        return postService.getPosts(boardId).stream().map(post -> postMapper.apply(post)).collect(Collectors.toList());
    }
    
    @RequestMapping(value = "/posts/{id}", method = RequestMethod.GET)
    public PostRepresentation getPost(@PathVariable Long id) {
        return postMapper.apply(postService.getPost(id));
    }
    
    @RequestMapping(value = "/posts/{id}/operations", method = RequestMethod.GET)
    public List<ResourceOperationRepresentation> getPostOperations(@PathVariable Long id) {
        return resourceService.getResourceOperations(id).stream().map(resourceOperation -> resourceOperationMapper.apply(resourceOperation)).collect(Collectors.toList());
    }
    
    @RequestMapping(value = "/posts/{id}", method = RequestMethod.PATCH)
    public PostRepresentation updatePost(@PathVariable Long id, @RequestBody @Valid PostPatchDTO postDTO) {
        return postMapper.apply(postService.executeAction(id, Action.EDIT, postDTO));
    }
    
    @RequestMapping(value = "/posts/{id}/accept", method = RequestMethod.POST)
    public PostRepresentation acceptPost(@PathVariable Long id, @RequestBody @Valid PostPatchDTO postDTO) {
        return postMapper.apply(postService.executeAction(id, Action.ACCEPT, postDTO));
    }
    
    @RequestMapping(value = "/posts/{id}/suspend", method = RequestMethod.POST)
    public PostRepresentation suspendPost(@PathVariable Long id, @RequestBody @Valid PostPatchDTO postDTO) {
        return postMapper.apply(postService.executeAction(id, Action.SUSPEND, postDTO));
    }
    
    @RequestMapping(value = "/posts/{id}/correct", method = RequestMethod.POST)
    public PostRepresentation correctPost(@PathVariable Long id, @RequestBody @Valid PostPatchDTO postDTO) {
        return postMapper.apply(postService.executeAction(id, Action.CORRECT, postDTO));
    }
    
    @RequestMapping(value = "/posts/{id}/reject", method = RequestMethod.POST)
    public PostRepresentation rejectPost(@PathVariable Long id, @RequestBody @Valid PostPatchDTO postDTO) {
        return postMapper.apply(postService.executeAction(id, Action.REJECT, postDTO));
    }
    
    @RequestMapping(value = "/posts/{id}/withdraw", method = RequestMethod.POST)
    public PostRepresentation withdrawPost(@PathVariable Long id, @RequestBody @Valid PostPatchDTO postDTO) {
        return postMapper.apply(postService.executeAction(id, Action.WITHDRAW, postDTO));
    }
    
    @RequestMapping(value = "/posts/{id}/withdraw", method = RequestMethod.POST)
    public PostRepresentation restorePost(@PathVariable Long id, @RequestBody @Valid PostPatchDTO postDTO) {
        return postMapper.apply(postService.executeAction(id, Action.RESTORE, postDTO));
    }
    
    @ExceptionHandler(ApiException.class)
    @ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
    public Map<String, String> handleException(ApiException apiException) {
        return ImmutableMap.of("exceptionCode", apiException.getExceptionCode().name());
    }
    
}
