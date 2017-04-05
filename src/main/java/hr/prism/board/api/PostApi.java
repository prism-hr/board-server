package hr.prism.board.api;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import hr.prism.board.domain.Post;
import hr.prism.board.dto.PostDTO;
import hr.prism.board.enums.Action;
import hr.prism.board.exception.ApiException;
import hr.prism.board.mapper.PostMapper;
import hr.prism.board.representation.PostRepresentation;
import hr.prism.board.service.PostService;
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
    
    @RequestMapping(value = "/boards/{id}/posts", method = RequestMethod.POST)
    public PostRepresentation postPost(@PathVariable Long id, @RequestBody @Valid PostDTO postDTO) {
        Post post = postService.createPost(id, postDTO);
        return postMapper.create(ImmutableSet.of("actions")).apply(post);
    }
    
    @RequestMapping(value = "/boards/{boardId}/posts", method = RequestMethod.GET)
    public List<PostRepresentation> getPosts(@PathVariable Long boardId) {
        return postService.findAllByUserOrderByUpdatedTimestamp().stream().map(post -> postMapper.create().apply(post)).collect(Collectors.toList());
    }
    
    @RequestMapping(value = "/posts/{id}", method = RequestMethod.GET)
    public PostRepresentation getPost(@PathVariable Long id) {
        return postMapper.create(ImmutableSet.of("actions")).apply(postService.findOne(id));
    }
    
    @RequestMapping(value = "/posts/{id}", method = RequestMethod.PUT)
    public void updatePost(@PathVariable Long id, @RequestBody @Valid PostDTO postDTO) {
        postService.executeAction(id, Action.EDIT, postDTO);
    }
    
    @RequestMapping(value = "/posts/{id}/accept", method = RequestMethod.PUT)
    public List<Action> acceptPost(@PathVariable Long id, @RequestBody @Valid PostDTO postDTO) {
        return postService.executeAction(id, Action.ACCEPT, postDTO);
    }
    
    @RequestMapping(value = "/posts/{id}/suspend", method = RequestMethod.PUT)
    public List<Action> suspendPost(@PathVariable Long id, @RequestBody @Valid PostDTO postDTO) {
        return postService.executeAction(id, Action.SUSPEND, postDTO);
    }
    
    @RequestMapping(value = "/posts/{id}/reject", method = RequestMethod.PUT)
    public List<Action> rejectPost(@PathVariable Long id, @RequestBody @Valid PostDTO postDTO) {
        return postService.executeAction(id, Action.REJECT, postDTO);
    }
    
    @ExceptionHandler(ApiException.class)
    @ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
    public Map<String, String> handleException(ApiException apiException) {
        return ImmutableMap.of("exceptionCode", apiException.getExceptionCode().name());
    }
    
}
