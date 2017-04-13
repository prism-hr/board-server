package hr.prism.board.service;

import hr.prism.board.domain.*;
import hr.prism.board.dto.*;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.CategoryType;
import hr.prism.board.enums.State;
import hr.prism.board.exception.ApiException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.repository.CategoryRepository;
import hr.prism.board.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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
    private ResourceService resourceService;
    
    @Inject
    private UserRoleService userRoleService;
    
    @Inject
    private UserService userService;
    
    @Inject
    private ActionService actionService;
    
    public Post getPost(Long id) {
        User currentUser = userService.getCurrentUser();
        Post post = (Post) resourceService.getResource(currentUser, Scope.POST, id);
        return (Post) actionService.executeAction(currentUser, post, Action.VIEW, () -> post);
    }
    
    public List<Post> getPosts(Long boardId) {
        User currentUser = userService.getCurrentUser();
        return resourceService.getResources(currentUser,
            new ResourceFilterDTO()
                .setScope(Scope.POST)
                .setParentId(boardId)
                .setOrderStatement("order by resource.updatedTimestamp desc"))
            .stream().map(resource -> (Post) resource).collect(Collectors.toList());
    }
    
    public Post createPost(Long boardId, PostDTO postDTO) {
        User currentUser = userService.getCurrentUserSecured();
        Board board = (Board) resourceService.getResource(currentUser, Scope.BOARD, boardId);
        Post createdPost = (Post) actionService.executeAction(currentUser, board, Action.EXTEND, () -> {
            Post post = new Post();
            Department department = (Department) board.getParent();
    
            post.setName(postDTO.getName());
            post.setDescription(postDTO.getDescription());
            post.setOrganizationName(postDTO.getOrganizationName());
            post.setExistingRelation(postDTO.getExistingRelation());
            post.setExistingRelationExplanation(postDTO.getExistingRelationExplanation());
            post.setApplyWebsite(postDTO.getApplyWebsite());
            post.setApplyEmail(postDTO.getApplyEmail());
    
            if (postDTO.getApplyDocument() != null) {
                post.setApplyDocument(documentService.getOrCreateDocument(postDTO.getApplyDocument()));
            }
    
            post.setLocation(locationService.getOrCreateLocation(postDTO.getLocation()));
            post = postRepository.save(post);
    
            updateCategories(post, CategoryType.POST, postDTO.getPostCategories(), board);
            updateCategories(post, CategoryType.MEMBER, postDTO.getMemberCategories(), department);
            resourceService.createResourceRelation(board, post);
            userRoleService.createUserRole(post, currentUser, Role.ADMINISTRATOR);
            return post;
        });
        
        if (createdPost.getState() == State.DRAFT && createdPost.getExistingRelation() == null) {
            throw new ApiException(ExceptionCode.MISSING_RELATION_DESCRIPTION);
        }
        
        return createdPost;
    }
    
    public Post executeAction(Long id, Action action, PostPatchDTO postDTO) {
        User currentUser = userService.getCurrentUserSecured();
        Post post = (Post) resourceService.getResource(currentUser, Scope.POST, id);
        return (Post) actionService.executeAction(currentUser, post, action, () -> {
            if (action == Action.EDIT) {
                updatePost(post, postDTO);
            } else {
                actionService.executeAction(currentUser, post, Action.EDIT, () -> {
                    updatePost(post, postDTO);
                    return post;
                });
            }
    
            return post;
        });
    }
    
    void updatePost(Post post, PostPatchDTO postDTO) {
        Board board = (Board) post.getParent();
        Department department = (Department) board.getParent();
        Optional<String> nameOptional = postDTO.getName();
        if (nameOptional != null) {
            if (nameOptional.isPresent()) {
                post.setName(nameOptional.get());
            } else {
                throw new IllegalStateException("Attempted to set post name to null");
            }
        }
        
        Optional<String> descriptionOptional = postDTO.getDescription();
        if (descriptionOptional != null) {
            if (descriptionOptional.isPresent()) {
                post.setDescription(descriptionOptional.get());
            } else {
                throw new IllegalStateException("Attempted to set post description to null");
            }
        }
        
        Optional<String> organizationNameOptional = postDTO.getOrganizationName();
        if (organizationNameOptional != null) {
            if (organizationNameOptional.isPresent()) {
                post.setOrganizationName(organizationNameOptional.get());
            } else {
                throw new IllegalStateException("Attempted to set post organization name to null");
            }
        }
        
        Optional<LocationDTO> locationOptional = postDTO.getLocation();
        if (locationOptional != null) {
            if (locationOptional.isPresent()) {
                String oldLocationId = Optional.ofNullable(post.getLocation()).map(Location::getGoogleId).orElse(null);
                String newLocationId = locationOptional.map(LocationDTO::getGoogleId).orElse(null);
                if (!Objects.equals(oldLocationId, newLocationId)) {
                    post.setLocation(locationService.getOrCreateLocation(locationOptional.orElse(null)));
                }
            } else {
                throw new IllegalStateException("Attempted to set post location to null");
            }
        }
        
        Optional<String> applyWebsiteOptional = postDTO.getApplyWebsite();
        if (applyWebsiteOptional != null) {
            String applyWebsite = applyWebsiteOptional.get();
            post.setApplyWebsite(applyWebsite);
            if (applyWebsite != null) {
                post.setApplyEmail(null);
                removeApplyDocument(post);
            }
        }
        
        Optional<String> applyEmailOptional = postDTO.getApplyEmail();
        if (applyEmailOptional != null) {
            String applyEmail = applyEmailOptional.get();
            post.setApplyEmail(applyEmail);
            if (applyEmail != null) {
                post.setApplyWebsite(null);
                removeApplyDocument(post);
            }
        }
        
        // update applyDocument
        Optional<DocumentDTO> applyDocumentOptional = postDTO.getApplyDocument();
        if (applyDocumentOptional != null) {
            String oldApplyDocumentId = Optional.ofNullable(post.getApplyDocument()).map(Document::getCloudinaryId).orElse(null);
            String newApplyDocumentId = applyDocumentOptional.map(DocumentDTO::getCloudinaryId).orElse(null);
            if (!Objects.equals(oldApplyDocumentId, newApplyDocumentId)) {
                post.setApplyDocument(documentService.getOrCreateDocument(applyDocumentOptional.orElse(null)));
                if (newApplyDocumentId != null) {
                    post.setApplyWebsite(null);
                    post.setApplyEmail(null);
                }
            }
        }
        
        if (post.getApplyWebsite() == null && post.getApplyEmail() == null && post.getApplyDocument() == null) {
            throw new IllegalStateException("Attempted to set post application mechanism to null");
        }
        
        if (postDTO.getPostCategories() != null) {
            updateCategories(post, CategoryType.POST, postDTO.getPostCategories().orElse(null), board);
        }
        
        if (postDTO.getMemberCategories() != null) {
            updateCategories(post, CategoryType.MEMBER, postDTO.getMemberCategories().orElse(null), department);
        }
    }
    
    private void updateCategories(Post post, CategoryType categoryType, List<String> categories, Resource parentResource) {
        categoryRepository.deleteByResourceAndType(post, categoryType);
        Set<ResourceCategory> savedCategories = post.getCategories();
        savedCategories.removeIf(next -> next.getType() == categoryType);
    
        List<ResourceCategory> newCategories = categoryRepository.findByResourceAndTypeAndNameIn(parentResource, categoryType, categories);
        newCategories.forEach(category -> {
            ResourceCategory insertCategory = new ResourceCategory().setResource(post).setName(category.getName()).setActive(true).setType(category.getType());
            insertCategory.setCreatedTimestamp(LocalDateTime.now());
            insertCategory = categoryRepository.save(insertCategory);
            savedCategories.add(insertCategory);
        });
    }
    
    private void removeApplyDocument(Post post) {
        Document applyDocument = post.getApplyDocument();
        post.setApplyDocument(null);
        if (applyDocument != null) {
            documentService.deleteDocument(applyDocument);
        }
    }
    
}
