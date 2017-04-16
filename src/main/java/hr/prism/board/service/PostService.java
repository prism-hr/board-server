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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.*;
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
            post.setLiveTimestamp(postDTO.getLiveTimestamp());
            post.setDeadTimestamp(postDTO.getDeadTimestamp());
    
            validatePost(post);
            post = postRepository.save(post);
    
            updateCategories(post, CategoryType.POST, postDTO.getPostCategories(), board);
            updateCategories(post, CategoryType.MEMBER, postDTO.getMemberCategories(), department);
            resourceService.createResourceRelation(board, post);
            userRoleService.createUserRole(post, currentUser, Role.ADMINISTRATOR);
            return post;
        });
        
        if (createdPost.getState() == State.DRAFT && createdPost.getExistingRelation() == null) {
            throw new ApiException(ExceptionCode.MISSING_POST_EXISTING_RELATION);
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
    
    @Scheduled(fixedDelay = 60000, fixedRate = 60000)
    public void publishAndRetirePosts() {
        LocalDateTime baseline = LocalDateTime.now();
        postRepository.findPostsToRetire(State.ACCEPTED, baseline).forEach(post -> {
        
        });
    
        postRepository.findPostsToPublish(State.PENDING, baseline).forEach(post -> {
        
        });
    }
    
    void updatePost(Post post, PostPatchDTO postDTO) {
        Board board = (Board) post.getParent();
        Department department = (Department) board.getParent();
    
        if (postDTO.getName() != null) {
            post.setName(postDTO.getName().orElse(null));
        }
    
        if (postDTO.getDescription() != null) {
            post.setDescription(postDTO.getDescription().orElse(null));
        }
    
        if (postDTO.getOrganizationName() != null) {
            post.setOrganizationName(postDTO.getOrganizationName().orElse(null));
        }
    
        // update location
        if (postDTO.getLocation() != null) {
            String newLocationId = postDTO.getLocation().map(LocationDTO::getGoogleId).orElse(null);
            String oldLocationId = Optional.ofNullable(post.getLocation()).map(Location::getGoogleId).orElse(null);
            if (!Objects.equals(newLocationId, oldLocationId)) {
                post.setLocation(locationService.getOrCreateLocation(postDTO.getLocation().orElse(null)));
            }
        }
        
        Optional<String> applyWebsiteOptional = postDTO.getApplyWebsite();
        if (applyWebsiteOptional != null) {
            if (applyWebsiteOptional.isPresent()) {
                post.setApplyWebsite(applyWebsiteOptional.get());
                post.setApplyEmail(null);
                removeApplyDocument(post);
            } else {
                post.setApplyWebsite(null);
            }
        }
        
        // update applyDocument
        Optional<DocumentDTO> applyDocumentOptional = postDTO.getApplyDocument();
        if (applyDocumentOptional != null) {
            if (applyDocumentOptional.isPresent()) {
                DocumentDTO newApplyDocumentDTO = applyDocumentOptional.get();
                String oldApplyDocumentId = Optional.ofNullable(post.getApplyDocument()).map(Document::getCloudinaryId).orElse(null);
                if (!Objects.equals(newApplyDocumentDTO.getCloudinaryId(), oldApplyDocumentId)) {
                    if (oldApplyDocumentId != null) {
                        removeApplyDocument(post);
                    }
    
                    post.setApplyDocument(documentService.getOrCreateDocument(newApplyDocumentDTO));
                }
    
                post.setApplyWebsite(null);
                post.setApplyEmail(null);
            } else {
                removeApplyDocument(post);
            }
        }
    
        Optional<String> applyEmailOptional = postDTO.getApplyEmail();
        if (applyEmailOptional != null) {
            if (applyEmailOptional.isPresent()) {
                post.setApplyEmail(applyEmailOptional.get());
                post.setApplyWebsite(null);
                removeApplyDocument(post);
            } else {
                post.setApplyEmail(null);
            }
        }
        if (postDTO.getApplyEmail() != null) {
            post.setApplyEmail(postDTO.getApplyEmail().orElse(null));
        }
        
        if (postDTO.getPostCategories() != null) {
            updateCategories(post, CategoryType.POST, postDTO.getPostCategories().orElse(null), board);
        }
        
        if (postDTO.getMemberCategories() != null) {
            updateCategories(post, CategoryType.MEMBER, postDTO.getMemberCategories().orElse(null), department);
        }
    
        if (postDTO.getLiveTimestamp() != null) {
            post.setLiveTimestamp(postDTO.getLiveTimestamp().orElse(null));
        }
    
        if (postDTO.getDeadTimestamp() != null) {
            post.setDeadTimestamp(postDTO.getDeadTimestamp().orElse(null));
        }
    
        validatePost(post);
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
    
    private void validatePost(Post post) {
        if (post.getName() == null) {
            throw new ApiException(ExceptionCode.MISSING_POST_NAME);
        } else if (post.getDescription() == null) {
            throw new ApiException(ExceptionCode.MISSING_POST_DESCRIPTION);
        } else if (post.getOrganizationName() == null) {
            throw new ApiException(ExceptionCode.MISSING_POST_ORGANIZATION_NAME);
        } else if (post.getLocation() == null) {
            throw new ApiException(ExceptionCode.MISSING_POST_LOCATION);
        }
        
        int nonNullCount = 0;
        for (Object applyMechanism : Arrays.asList(post.getApplyWebsite(), post.getApplyDocument(), post.getApplyEmail())) {
            if (applyMechanism != null) {
                nonNullCount++;
            }
        }
        
        if (nonNullCount != 1) {
            throw new ApiException(ExceptionCode.CORRUPTED_POST_APPLY_MECHANISM);
        } else if (post.getLiveTimestamp() == null) {
            throw new ApiException(ExceptionCode.MISSING_POST_LIVE_TIMESTAMP);
        } else if (post.getDeadTimestamp() == null) {
            throw new ApiException(ExceptionCode.MISSING_POST_DEAD_TIMESTAMP);
        }
    }
    
}
