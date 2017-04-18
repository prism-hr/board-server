package hr.prism.board.service;

import hr.prism.board.domain.*;
import hr.prism.board.dto.*;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.CategoryType;
import hr.prism.board.enums.State;
import hr.prism.board.exception.ApiException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.mapper.DocumentMapper;
import hr.prism.board.mapper.LocationMapper;
import hr.prism.board.repository.PostRepository;
import hr.prism.board.repository.ResourceCategoryRepository;
import hr.prism.board.representation.ResourceChangeListRepresentation;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class PostService {
    
    @Inject
    private PostRepository postRepository;
    
    @Inject
    private ResourceCategoryRepository resourceCategoryRepository;
    
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
    
    @Inject
    private DocumentMapper documentMapper;
    
    @Inject
    private LocationMapper locationMapper;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Inject
    @SuppressWarnings("SpringJavaAutowiringInspection")
    private PlatformTransactionManager platformTransactionManager;
    
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
    public void publishAndRetirePostsScheduled() {
        publishAndRetirePosts();
    }
    
    void updatePost(Post post, PostPatchDTO postDTO) {
        Board board = (Board) post.getParent();
        Department department = (Department) board.getParent();
        ResourceChangeListRepresentation changeList = new ResourceChangeListRepresentation();
        
        if (postDTO.getName() != null) {
            String oldName = post.getName();
            post.setName(postDTO.getName().orElse(null));
            changeList.put("name", oldName, post.getName());
        }
    
        if (postDTO.getDescription() != null) {
            String oldDescription = post.getDescription();
            post.setDescription(postDTO.getDescription().orElse(null));
            changeList.put("description", oldDescription, post.getDescription());
        }
    
        if (postDTO.getOrganizationName() != null) {
            String oldOrganizationName = post.getOrganizationName();
            post.setOrganizationName(postDTO.getOrganizationName().orElse(null));
            changeList.put("organizationName", oldOrganizationName, post.getOrganizationName());
        }
    
        if (postDTO.getLocation() != null) {
            Location oldLocation = post.getLocation();
            String oldLocationId = Optional.ofNullable(oldLocation).map(Location::getGoogleId).orElse(null);
            String newLocationId = postDTO.getLocation().map(LocationDTO::getGoogleId).orElse(null);
            if (!Objects.equals(newLocationId, oldLocationId)) {
                post.setLocation(locationService.getOrCreateLocation(postDTO.getLocation().orElse(null)));
                changeList.put("location", locationMapper.apply(oldLocation), locationMapper.apply(post.getLocation()));
            }
        }
        
        Optional<String> applyWebsiteOptional = postDTO.getApplyWebsite();
        Optional<DocumentDTO> applyDocumentOptional = postDTO.getApplyDocument();
        Optional<String> applyEmailOptional = postDTO.getApplyEmail();
        if (applyWebsiteOptional != null || applyDocumentOptional != null || applyEmailOptional != null) {
            String oldApplyWebsite = post.getApplyWebsite();
            Document oldApplyDocument = post.getApplyDocument();
            String oldApplyEmail = post.getApplyEmail();
        
            if (applyWebsiteOptional != null) {
                if (applyWebsiteOptional.isPresent()) {
                    post.setApplyWebsite(applyWebsiteOptional.get());
                    post.setApplyDocument(null);
                    post.setApplyEmail(null);
                } else {
                    post.setApplyWebsite(null);
                }
            }
        
            if (applyDocumentOptional != null) {
                if (applyDocumentOptional.isPresent()) {
                    DocumentDTO newApplyDocumentDTO = applyDocumentOptional.get();
                    String oldApplyDocumentId = Optional.ofNullable(post.getApplyDocument()).map(Document::getCloudinaryId).orElse(null);
                    if (!Objects.equals(newApplyDocumentDTO.getCloudinaryId(), oldApplyDocumentId)) {
                        post.setApplyDocument(documentService.getOrCreateDocument(newApplyDocumentDTO));
                    }
                
                    post.setApplyWebsite(null);
                    post.setApplyEmail(null);
                } else {
                    post.setApplyDocument(null);
                }
            }
        
            if (applyEmailOptional != null) {
                if (applyEmailOptional.isPresent()) {
                    post.setApplyEmail(applyEmailOptional.get());
                    post.setApplyWebsite(null);
                    post.setApplyDocument(null);
                } else {
                    post.setApplyEmail(null);
                }
            }
        
            changeList.put("applyWebsite", oldApplyWebsite, post.getApplyWebsite());
            changeList.put("applyDocument", documentMapper.apply(oldApplyDocument), documentMapper.apply(post.getApplyDocument()));
            changeList.put("applyEmail", oldApplyEmail, post.getApplyEmail());
        }
        
        if (postDTO.getPostCategories() != null) {
            List<String> oldPostCategories = resourceService.getCategories(post, CategoryType.POST);
            updateCategories(post, CategoryType.POST, postDTO.getPostCategories().orElse(null), board);
            changeList.put("postCategories", oldPostCategories, resourceService.getCategories(post, CategoryType.POST));
        }
        
        if (postDTO.getMemberCategories() != null) {
            List<String> oldMemberCategories = resourceService.getCategories(post, CategoryType.MEMBER);
            updateCategories(post, CategoryType.MEMBER, postDTO.getMemberCategories().orElse(null), department);
            changeList.put("memberCategories", oldMemberCategories, resourceService.getCategories(post, CategoryType.MEMBER));
        }
    
        if (postDTO.getLiveTimestamp() != null) {
            LocalDateTime oldLiveTimestamp = post.getLiveTimestamp();
            post.setLiveTimestamp(postDTO.getLiveTimestamp().orElse(null));
            changeList.put("liveTimestamp", oldLiveTimestamp, post.getLiveTimestamp());
        }
    
        if (postDTO.getDeadTimestamp() != null) {
            LocalDateTime oldDeadTimestamp = post.getDeadTimestamp();
            post.setDeadTimestamp(postDTO.getDeadTimestamp().orElse(null));
            changeList.put("deadTimestamp", oldDeadTimestamp, post.getDeadTimestamp());
        }
    
        validatePost(post);
        post.setChangeList(changeList);
        post.setComment(postDTO.getComment());
    }
    
    void publishAndRetirePosts() {
        LocalDateTime baseline = LocalDateTime.now();
        List<Long> postToRetireIds = postRepository.findPostsToRetire(State.ACCEPTED, baseline);
        List<Long> postToPublishIds = postRepository.findPostsToPublish(State.PENDING, baseline);
        executeActions(postToRetireIds, Action.RETIRE, State.EXPIRED, baseline);
        executeActions(postToPublishIds, Action.PUBLISH, State.ACCEPTED, baseline);
    }
    
    private void updateCategories(Post post, CategoryType categoryType, List<String> categories, Resource parentResource) {
        resourceCategoryRepository.deleteByResourceAndType(post, categoryType);
        Set<ResourceCategory> savedCategories = post.getCategories();
        savedCategories.removeIf(next -> next.getType() == categoryType);
    
        List<ResourceCategory> newCategories = resourceCategoryRepository.findByResourceAndTypeAndNameIn(parentResource, categoryType, categories);
        newCategories.forEach(category -> {
            ResourceCategory insertCategory = new ResourceCategory().setResource(post).setName(category.getName()).setActive(true).setType(category.getType());
            insertCategory.setCreatedTimestamp(LocalDateTime.now());
            insertCategory = resourceCategoryRepository.save(insertCategory);
            savedCategories.add(insertCategory);
        });
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
    
    @SuppressWarnings("JpaQlInspection")
    private void executeActions(List<Long> postIds, Action action, State state, LocalDateTime baseline) {
        if (postIds.size() > 0) {
            TransactionTemplate transactionTemplate = new TransactionTemplate(platformTransactionManager);
            transactionTemplate.execute(status -> {
                entityManager.createQuery(
                    "update Post post " +
                        "set post.previousState = post.state, " +
                        "post.state = :state, " +
                        "post.updatedTimestamp = :baseline " +
                        "where post.id in (:postIds)")
                    .setParameter("state", state)
                    .setParameter("baseline", baseline)
                    .setParameter("postIds", postIds)
                    .executeUpdate();
                
                entityManager.createNativeQuery(
                    "insert into resource_operation (resource_id, action, created_timestamp) " +
                        "select post.id as resource_id, :action as action, :baseline as created_timestamp " +
                        "from post " +
                        "where post.id in (:postIds) " +
                        "order by post.id")
                    .setParameter("action", action)
                    .setParameter("baseline", baseline)
                    .setParameter("postIds", postIds)
                    .executeUpdate();
                return null;
            });
        }
    }
    
}
