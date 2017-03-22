package hr.prism.board.api;

import hr.prism.board.authentication.Restriction;
import hr.prism.board.domain.Post;
import hr.prism.board.domain.Role;
import hr.prism.board.domain.Scope;
import hr.prism.board.dto.PostDTO;
import hr.prism.board.mapper.PostMapper;
import hr.prism.board.representation.PostRepresentation;
import hr.prism.board.service.PostService;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class PostApi {

    @Inject
    private PostService postService;

    @Inject
    private PostMapper postMapper;

    @RequestMapping(value = "/boards/{boardId}/posts", method = RequestMethod.POST)
    public PostRepresentation postPost(@PathVariable Long boardId, @RequestBody PostDTO postDTO) {
        Post post = postService.createPost(boardId, postDTO);
        return postMapper.apply(post);
    }

    @RequestMapping(value = "/boards/{boardId}/posts", method = RequestMethod.GET)
    public List<PostRepresentation> getPosts(@PathVariable Long boardId) {
        return postService.findByBoard(boardId).stream().map(postMapper).collect(Collectors.toList());
    }

    @RequestMapping(value = "/posts/{id}", method = RequestMethod.GET)
    public PostRepresentation getPost(@PathVariable("id") Long id) {
        return postMapper.apply(postService.findOne(id));
    }

    @Restriction(scope = Scope.POST, roles = Role.ADMINISTRATOR)
    @RequestMapping(value = "/posts/{id}", method = RequestMethod.PUT)
    public void updatePost(@PathVariable("id") Long id, @RequestBody PostDTO postDTO) {
        postService.updatePost(id, postDTO);
    }

}
