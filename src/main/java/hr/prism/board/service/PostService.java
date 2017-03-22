package hr.prism.board.service;

import hr.prism.board.domain.*;
import hr.prism.board.dto.DocumentDTO;
import hr.prism.board.dto.LocationDTO;
import hr.prism.board.dto.PostDTO;
import hr.prism.board.exception.ApiException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
public class PostService {

    @Inject
    private PostRepository postRepository;

    @Inject
    private DocumentService documentService;

    @Inject
    private LocationService locationService;

    @Inject
    private BoardService boardService;

    @Inject
    private ResourceService resourceService;

    @Inject
    private UserRoleService userRoleService;

    @Inject
    private UserService userService;


    public List<Post> findByBoard(Long boardId) {
        return postRepository.findByBoard(boardId);
    }

    public Post createPost(Long boardId, PostDTO postDTO) {
        Board board = boardService.findOne(boardId);

        Post post = new Post();
        validateNameUniqueness(post.getName(), board);
        updateSimpleFields(post, postDTO);

        if (postDTO.getApplyDocument() != null) {
            post.setApplyDocument(documentService.getOrCreateDocument(postDTO.getApplyDocument()));
        }

        post.setLocation(locationService.getOrCreateLocation(postDTO.getLocation()));
        post = postRepository.save(post);
        resourceService.createResourceRelation(post, post);
        resourceService.createResourceRelation(board, post);
        userRoleService.createUserRole(board, userService.getCurrentUser(), Role.ADMINISTRATOR);
        return post;
    }

    public void updatePost(Long postId, PostDTO postDTO) {
        Post post = postRepository.findOne(postId);
        updateSimpleFields(post, postDTO);

        // update applyDocument
        String existingApplyDocumentId = Optional.ofNullable(post.getApplyDocument()).map(Document::getCloudinaryId).orElse(null);
        String newApplyDocumentId = Optional.ofNullable(postDTO.getApplyDocument()).map(DocumentDTO::getCloudinaryId).orElse(null);
        if (!Objects.equals(existingApplyDocumentId, newApplyDocumentId)) {
            post.setApplyDocument(documentService.getOrCreateDocument(postDTO.getApplyDocument()));
        }

        // update location
        String existingLocationId = Optional.ofNullable(post.getLocation()).map(Location::getGoogleId).orElse(null);
        String newLocationId = Optional.ofNullable(postDTO.getLocation()).map(LocationDTO::getGoogleId).orElse(null);
        if (!Objects.equals(existingLocationId, newLocationId)) {
            post.setLocation(locationService.getOrCreateLocation(postDTO.getLocation()));
        }
    }

    public Post findOne(Long id) {
        return postRepository.findOne(id);
    }

    private void validateNameUniqueness(String name, Board board) {
        Post post = postRepository.findByNameAndBoard(name, board);
        if (post != null) {
            throw new ApiException(ExceptionCode.DUPLICATE_POST);
        }
    }

    private void updateSimpleFields(Post post, PostDTO postDTO) {
        post.setName(postDTO.getName());
        post.setDescription(postDTO.getDescription());
        post.setOrganizationName(postDTO.getOrganizationName());
        post.setExistingRelation(postDTO.getExistingRelation());
        post.setApplyWebsite(postDTO.getApplyWebsite());
        post.setApplyEmail(postDTO.getApplyEmail());
    }
}
