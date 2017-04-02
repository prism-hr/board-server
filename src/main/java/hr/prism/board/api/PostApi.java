package hr.prism.board.api;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import hr.prism.board.authentication.Restriction;
import hr.prism.board.domain.Post;
import hr.prism.board.domain.Scope;
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
    
    @Restriction(scope = Scope.BOARD, actions = Action.AUGMENT)
    @RequestMapping(value = "/boards/{id}/posts", method = RequestMethod.POST)
    public PostRepresentation postPost(@PathVariable Long id, @RequestBody @Valid PostDTO postDTO) {
        Post post = postService.createPost(id, postDTO);
        return postMapper.create(ImmutableSet.of("roles")).apply(post);
    }
    
    @Restriction(scope = Scope.POST)
    @RequestMapping(value = "/boards/{boardId}/posts", method = RequestMethod.GET)
    public List<PostRepresentation> getPosts(@PathVariable Long boardId) {
        return postService.findByBoard(boardId).stream().map(post -> postMapper.create().apply(post)).collect(Collectors.toList());
    }
    
    @Restriction(scope = Scope.POST)
    @RequestMapping(value = "/posts/{id}", method = RequestMethod.GET)
    public PostRepresentation getPost(@PathVariable("id") Long id) {
        return postMapper.create(ImmutableSet.of("roles")).apply(postService.findOne(id));
    }
    
    @Restriction(scope = Scope.POST, actions = Action.EDIT)
    @RequestMapping(value = "/posts/{id}", method = RequestMethod.PUT)
    public void updatePost(@PathVariable("id") Long id, @RequestBody @Valid PostDTO postDTO) {
        postService.executeAction(id, Action.EDIT, postDTO);
    }
    
    @Restriction(scope = Scope.BOARD, actions = Action.ACCEPT)
    @RequestMapping(value = "/posts/{id}/accept", method = RequestMethod.PUT)
    public void acceptPost(@PathVariable("id") Long id, @RequestBody @Valid PostDTO postDTO) {
        postService.executeAction(id, Action.ACCEPT, postDTO);
    }
    
    @Restriction(scope = Scope.BOARD, actions = Action.SUSPEND)
    @RequestMapping(value = "/posts/{id}/suspend", method = RequestMethod.PUT)
    public void suspendPost(@PathVariable("id") Long id, @RequestBody @Valid PostDTO postDTO) {
        postService.executeAction(id, Action.SUSPEND, postDTO);
    }
    
    @Restriction(scope = Scope.BOARD, actions = Action.REJECT)
    @RequestMapping(value = "/posts/{id}/reject", method = RequestMethod.PUT)
    public void rejectPost(@PathVariable("id") Long id, @RequestBody @Valid PostDTO postDTO) {
        postService.executeAction(id, Action.REJECT, postDTO);
    }
    
    @ExceptionHandler(ApiException.class)
    @ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
    public Map<String, String> handleException(ApiException apiException) {
        return ImmutableMap.of("exceptionCode", apiException.getExceptionCode().name());
    }
    
}
