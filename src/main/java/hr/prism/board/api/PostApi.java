package hr.prism.board.api;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import hr.prism.board.authentication.Restriction;
import hr.prism.board.domain.Post;
import hr.prism.board.domain.Role;
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

    @RequestMapping(value = "/boards/{boardId}/posts", method = RequestMethod.POST)
    public PostRepresentation postPost(@PathVariable Long boardId, @RequestBody @Valid PostDTO postDTO) {
        Post post = postService.createPost(boardId, postDTO);
        return postMapper.create(ImmutableSet.of("roles")).apply(post);
    }

    @RequestMapping(value = "/boards/{boardId}/posts", method = RequestMethod.GET)
    public List<PostRepresentation> getPosts(@PathVariable Long boardId) {
        return postService.findByBoard(boardId).stream().map(post -> postMapper.create().apply(post)).collect(Collectors.toList());
    }

    @RequestMapping(value = "/posts/{id}", method = RequestMethod.GET)
    public PostRepresentation getPost(@PathVariable("id") Long id) {
        return postMapper.create(ImmutableSet.of("roles")).apply(postService.findOne(id));
    }

    @Restriction(scope = Scope.POST, roles = Role.ADMINISTRATOR)
    @RequestMapping(value = "/posts/{id}", method = RequestMethod.PUT)
    public void updatePost(@PathVariable("id") Long id, @RequestBody @Valid PostDTO postDTO) {
        postService.executeAction(id, Action.EDIT, postDTO);
    }

    @Restriction(scope = Scope.BOARD, roles = Role.ADMINISTRATOR)
    @RequestMapping(value = "/posts/{id}/{action}", method = RequestMethod.PUT)
    public void executeAction(@PathVariable("id") Long id, @PathVariable("action") Action action, @RequestBody @Valid PostDTO postDTO) {
        postService.executeAction(id, action, postDTO);
    }

    @ExceptionHandler(ApiException.class)
    @ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
    public Map<String, String> handleException(ApiException apiException) {
        return ImmutableMap.of("exceptionCode", apiException.getExceptionCode().name());
    }

}
