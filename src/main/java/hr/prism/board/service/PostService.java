package hr.prism.board.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hr.prism.board.domain.*;
import hr.prism.board.dto.DocumentDTO;
import hr.prism.board.dto.PostDTO;
import hr.prism.board.dto.PostPatchDTO;
import hr.prism.board.dto.ResourceFilterDTO;
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
import org.apache.commons.collections.CollectionUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
    
    @Inject
    private ObjectMapper objectMapper;
    
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
            post.setExistingRelationExplanation(mapExistingRelationExplanation(postDTO.getExistingRelationExplanation()));
            post.setApplyWebsite(postDTO.getApplyWebsite());
            post.setApplyEmail(postDTO.getApplyEmail());
    
            if (postDTO.getApplyDocument() != null) {
                post.setApplyDocument(documentService.getOrCreateDocument(postDTO.getApplyDocument()));
            }
    
            post.setLocation(locationService.getOrCreateLocation(postDTO.getLocation()));
    
            LocalDateTime liveTimestamp = postDTO.getLiveTimestamp();
            LocalDateTime deadTimestamp = postDTO.getDeadTimestamp();
            if (liveTimestamp != null && deadTimestamp != null) {
                post.setLiveTimestamp(liveTimestamp.truncatedTo(ChronoUnit.SECONDS));
                post.setDeadTimestamp(deadTimestamp.truncatedTo(ChronoUnit.SECONDS));
            } else {
                LocalDateTime baseline = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
                post.setLiveTimestamp(baseline);
                post.setDeadTimestamp(baseline.plusWeeks(4));
            }
            
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
    
    @Scheduled(initialDelay = 60000, fixedRate = 60000)
    public void publishAndRetirePostsScheduled() {
        publishAndRetirePosts();
    }
    
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private void updatePost(Post post, PostPatchDTO postDTO) {
        ResourceChangeListRepresentation changeList = new ResourceChangeListRepresentation();
        post.setChangeList(changeList);
        
        resourceService.patchProperty(post, "name", postDTO.getName(), ExceptionCode.MISSING_POST_NAME);
        resourceService.patchProperty(post, "description", postDTO.getDescription(), ExceptionCode.MISSING_POST_DESCRIPTION);
        resourceService.patchProperty(post, "organizationName", postDTO.getOrganizationName(), ExceptionCode.MISSING_POST_ORGANIZATION_NAME);
        resourceService.patchLocation(post, postDTO.getLocation(), ExceptionCode.MISSING_POST_LOCATION);
        
        Optional<String> applyWebsiteOptional = postDTO.getApplyWebsite();
        Optional<DocumentDTO> applyDocumentOptional = postDTO.getApplyDocument();
        Optional<String> applyEmailOptional = postDTO.getApplyEmail();
        
        int applyOptionCount = 0;
        for (Optional<?> applyOption : new Optional<?>[]{applyWebsiteOptional, applyDocumentOptional, applyEmailOptional}) {
            if (applyOption.isPresent()) {
                applyOptionCount++;
            }
        }
        
        if (applyOptionCount == 0) {
            throw new ApiException(ExceptionCode.MISSING_POST_APPLY);
        } else if (applyOptionCount > 1) {
            throw new ApiException(ExceptionCode.CORRUPTED_POST_APPLY);
        }
        
        resourceService.patchProperty(post, "applyWebsite", applyWebsiteOptional, () -> {
            resourceService.patchDocument(post, "applyDocument", Optional.empty());
            resourceService.patchProperty(post, "applyEmail", Optional.empty());
        });
        
        resourceService.patchDocument(post, "applyDocument", applyDocumentOptional, () -> {
            resourceService.patchProperty(post, "applyWesbite", Optional.empty());
            resourceService.patchProperty(post, "applyEmail", Optional.empty());
        });
        
        resourceService.patchProperty(post, "applyEmail", applyWebsiteOptional, () -> {
            resourceService.patchDocument(post, "applyDocument", Optional.empty());
            resourceService.patchProperty(post, "applyWebsite", Optional.empty());
        });
        
        Board board = (Board) post.getParent();
        Department department = (Department) board.getParent();
        patchCategories(post, CategoryType.POST, postDTO.getPostCategories(), board);
        patchCategories(post, CategoryType.MEMBER, postDTO.getMemberCategories(), department);
        
        Optional<LocalDateTime> liveTimestampOptional = postDTO.getLiveTimestamp();
        Optional<LocalDateTime> deadTimestampOptional = postDTO.getDeadTimestamp();
        if (liveTimestampOptional != null && deadTimestampOptional != null) {
            if (liveTimestampOptional.isPresent() && deadTimestampOptional.isPresent()) {
                resourceService.patchProperty(post, "liveTimestamp", Optional.of(liveTimestampOptional.get().truncatedTo(ChronoUnit.SECONDS)));
                resourceService.patchProperty(post, "deadTimestamp", Optional.of(deadTimestampOptional.get().truncatedTo(ChronoUnit.SECONDS)));
            } else {
                LocalDateTime baseline = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
                resourceService.patchProperty(post, "liveTimestamp", Optional.of(baseline));
                resourceService.patchProperty(post, "deadTimestamp", Optional.of(baseline.plusWeeks(4)));
            }
        }
        
        post.setComment(postDTO.getComment());
    }
    
    synchronized void publishAndRetirePosts() {
        LocalDateTime baseline = LocalDateTime.now();
        List<Long> postToRetireIds = postRepository.findPostsToRetire(State.ACCEPTED, baseline);
        List<Long> postToPublishIds = postRepository.findPostsToPublish(State.PENDING, baseline);
        executeActions(postToRetireIds, Action.RETIRE, State.EXPIRED, baseline);
        executeActions(postToPublishIds, Action.PUBLISH, State.ACCEPTED, baseline);
    }
    
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private void patchCategories(Post post, CategoryType categoryType, Optional<List<String>> categories, Resource reference) {
        if (categories != null) {
            List<String> oldCategories = resourceService.getCategories(post, categoryType);
            if (categories.isPresent()) {
                List<String> newCategories = categories.get();
                newCategories.sort(Comparator.naturalOrder());
                if (!Objects.equals(oldCategories, newCategories)) {
                    updateCategories(post, categoryType, newCategories, reference);
                    post.getChangeList().put(categoryType.name().toLowerCase() + "Categories", oldCategories, resourceService.getCategories(post, categoryType));
                }
            } else if (oldCategories != null) {
                updateCategories(post, categoryType, null, reference);
                post.getChangeList().put(categoryType.name().toLowerCase() + "Categories", oldCategories, resourceService.getCategories(post, categoryType));
            }
        }
    }
    
    private void updateCategories(Post post, CategoryType categoryType, List<String> categories, Resource reference) {
        resourceCategoryRepository.deleteByResourceAndType(post, categoryType);
        Set<ResourceCategory> oldCategories = post.getCategories();
        oldCategories.removeIf(next -> next.getType() == categoryType);
        
        if (CollectionUtils.isNotEmpty(categories)) {
            List<ResourceCategory> newCategories = resourceCategoryRepository.findByResourceAndTypeAndNameIn(reference, categoryType, categories);
            newCategories.forEach(category -> {
                ResourceCategory newCategory = new ResourceCategory().setResource(post).setName(category.getName()).setActive(true).setType(categoryType);
                newCategory.setCreatedTimestamp(LocalDateTime.now());
                newCategory = resourceCategoryRepository.save(newCategory);
                oldCategories.add(newCategory);
            });
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
                    "INSERT INTO resource_operation (resource_id, action, created_timestamp) " +
                        "SELECT resource.id AS resource_id, :action AS action, :baseline AS created_timestamp " +
                        "FROM resource " +
                        "WHERE resource.id IN (:postIds) " +
                        "ORDER BY resource.id")
                    .setParameter("action", action)
                    .setParameter("baseline", baseline)
                    .setParameter("postIds", postIds)
                    .executeUpdate();
                return null;
            });
        }
    }
    
    private String mapExistingRelationExplanation(LinkedHashMap<String, Object> existingRelationExplanation) {
        if (existingRelationExplanation == null) {
            return null;
        }
        
        try {
            return objectMapper.writeValueAsString(existingRelationExplanation);
        } catch (JsonProcessingException e) {
            throw new ApiException(ExceptionCode.CORRUPTED_POST_EXISTING_RELATION_EXPLANATION, e);
        }
    }
    
}
