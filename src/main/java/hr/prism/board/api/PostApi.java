package hr.prism.board.api;

import hr.prism.board.domain.Post;
import hr.prism.board.dto.PostDTO;
import hr.prism.board.dto.PostPatchDTO;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.Scope;
import hr.prism.board.mapper.PostMapper;
import hr.prism.board.mapper.ResourceOperationMapper;
import hr.prism.board.representation.PostRepresentation;
import hr.prism.board.representation.ResourceOperationRepresentation;
import hr.prism.board.service.PostService;
import hr.prism.board.service.ResourceService;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
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
    public PostRepresentation getPost(@PathVariable Long id) {
        return postMapper.apply(postService.getPost(id));
    }

    @RequestMapping(value = "/api/posts/{id}/operations", method = RequestMethod.GET)
    public List<ResourceOperationRepresentation> getPostOperations(@PathVariable Long id) {
        return resourceService.getResourceOperations(Scope.POST, id).stream()
            .map(resourceOperation -> resourceOperationMapper.apply(resourceOperation)).collect(Collectors.toList());
    }

    @RequestMapping(value = "/api/posts/{id}", method = RequestMethod.PATCH)
    public PostRepresentation updatePost(@PathVariable Long id, @RequestBody @Valid PostPatchDTO postDTO) {
        return postMapper.apply(postService.executeAction(id, Action.EDIT, postDTO));
    }

    @RequestMapping(value = "/api/posts/{id}/actions/{action}", method = RequestMethod.POST)
    public PostRepresentation executeAction(@PathVariable Long id, @PathVariable String action, @RequestBody @Valid PostPatchDTO postDTO) {
        Action actionEnum = Action.valueOf(action.toUpperCase());
        return postMapper.apply(postService.executeAction(id, actionEnum, postDTO));
    }

}
