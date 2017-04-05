package hr.prism.board.service;

import hr.prism.board.domain.*;
import hr.prism.board.dto.DocumentDTO;
import hr.prism.board.dto.LocationDTO;
import hr.prism.board.dto.PostDTO;
import hr.prism.board.dto.ResourceFilterDTO;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.CategoryType;
import hr.prism.board.enums.State;
import hr.prism.board.exception.ApiException;
import hr.prism.board.exception.ApiForbiddenException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.repository.CategoryRepository;
import hr.prism.board.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class PostService {
    
    @Inject
    private PostRepository postRepository;
    
    @Inject
    private CategoryRepository categoryRepository;
    
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
    
    @Inject
    private ActionService actionService;
    
    public Post findOne(Long id) {
        return postRepository.findOne(id);
    }
    
    public List<Post> findAllByUserOrderByUpdatedTimestamp() {
        User currentUser = userService.getCurrentUser();
        return resourceService.getResources(currentUser, new ResourceFilterDTO().setScope(Scope.POST).setOrderStatement("order by resource.updatedTimestamp desc"))
            .stream().map(resource -> (Post) resource).collect(Collectors.toList());
    }
    
    public Post createPost(Long boardId, PostDTO postDTO) {
        User currentUser = userService.getCurrentUserSecured();
        Board board = (Board) resourceService.getResource(currentUser, Scope.BOARD, boardId);
        Optional<ResourceAction> augmentActionOptional = board.getResourceActions().stream().filter(resourceAction -> resourceAction.getAction() == Action.AUGMENT).findFirst();
        if (augmentActionOptional.isPresent()) {
            State state = augmentActionOptional.get().getState();
            if (state == State.DRAFT && postDTO.getExistingRelation() == null) {
                throw new ApiException(ExceptionCode.MISSING_RELATION_DESCRIPTION);
            }
        
            Post post = new Post();
            resourceService.updateState(post, state);
            updateSimpleFields(post, postDTO, board, (Department) board.getParent());
        
            if (postDTO.getApplyDocument() != null) {
                post.setApplyDocument(documentService.getOrCreateDocument(postDTO.getApplyDocument()));
            }
        
            post.setLocation(locationService.getOrCreateLocation(postDTO.getLocation()));
            post = postRepository.save(post);
        
            resourceService.createResourceRelation(board, post);
            userRoleService.createUserRole(post, currentUser, Role.ADMINISTRATOR);
            return (Post) resourceService.getResource(currentUser, Scope.POST, post.getId());
        }
    
        throw new ApiForbiddenException("User cannot augment " + board.toString());
    }
    
    public List<Action> executeAction(Long postId, Action action, PostDTO postDTO) {
        User currentUser = userService.getCurrentUserSecured();
        Post post = (Post) resourceService.getResource(currentUser, Scope.POST, postId);
        List<Action> actions = actionService.executeAction(post, action);
        updatePost(postId, postDTO);
        return actions;
    }
    
    private void updateSimpleFields(Post post, PostDTO postDTO, Board board, Department department) {
        post.setName(postDTO.getName());
        post.setDescription(postDTO.getDescription());
        post.setOrganizationName(postDTO.getOrganizationName());
        post.setExistingRelation(postDTO.getExistingRelation());
        post.setApplyWebsite(postDTO.getApplyWebsite());
        post.setApplyEmail(postDTO.getApplyEmail());
        
        // update categories
        List<ResourceCategory> postCategories = categoryRepository.findByResourceAndTypeAndNameIn(board, CategoryType.POST, postDTO.getPostCategories());
        List<ResourceCategory> memberCategories = categoryRepository.findByResourceAndTypeAndNameIn(department, CategoryType.MEMBER, postDTO.getMemberCategories());
        post.getPostCategories().clear();
        post.getPostCategories().addAll(postCategories);
        post.getPostCategories().addAll(memberCategories);
    }
    
    private void updatePost(Long postId, PostDTO postDTO) {
        Post post = postRepository.findOne(postId);
        Board board = (Board) post.getParent();
        updateSimpleFields(post, postDTO, board, (Department) board.getParent());
        
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
    
}
