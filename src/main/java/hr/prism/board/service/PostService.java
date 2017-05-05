package hr.prism.board.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
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
import hr.prism.board.repository.PostRepository;
import hr.prism.board.representation.ResourceChangeListRepresentation;
import hr.prism.board.util.BoardUtils;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.IOException;
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
    private DocumentService documentService;
    
    @Inject
    private LocationService locationService;
    
    @Inject
    private ResourceService resourceService;
    
    @Inject
    private ResourcePatchService resourcePatchService;
    
    @Inject
    private UserRoleService userRoleService;
    
    @Inject
    private UserService userService;
    
    @Inject
    private ActionService actionService;
    
    @Inject
    private ObjectMapper objectMapper;
    
    @PersistenceContext
    private EntityManager entityManager;
    
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
        return (Post) actionService.executeAction(currentUser, post.setComment(postDTO.getComment()), action, () -> {
            if (action == Action.EDIT) {
                updatePost(post, postDTO);
            } else if (postDTO.hasUpdates()) {
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
    
    public synchronized void publishAndRetirePosts() {
        LocalDateTime baseline = LocalDateTime.now();
        List<Long> postToRetireIds = postRepository.findPostsToRetire(State.ACCEPTED, baseline);
        List<Long> postToPublishIds = postRepository.findPostsToPublish(State.PENDING, baseline);
        executeActions(postToRetireIds, Action.RETIRE, State.EXPIRED, baseline);
        executeActions(postToPublishIds, Action.PUBLISH, State.ACCEPTED, baseline);
    }
    
    public LinkedHashMap<String, Object> mapExistingRelationExplanation(String existingRelationExplanation) {
        if (existingRelationExplanation == null) {
            return null;
        }
        
        try {
            return objectMapper.readValue(existingRelationExplanation, new TypeReference<LinkedHashMap<String, Object>>() {
            });
        } catch (IOException e) {
            throw new ApiException(ExceptionCode.CORRUPTED_POST_EXISTING_RELATION_EXPLANATION, e);
        }
    }
    
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private void updatePost(Post post, PostPatchDTO postDTO) {
        post.setChangeList(new ResourceChangeListRepresentation());
        resourcePatchService.patchProperty(post, "name", post::getName, post::setName, postDTO.getName());
        resourcePatchService.patchProperty(post, "description", post::getDescription, post::setDescription, postDTO.getDescription());
        resourcePatchService.patchProperty(post, "organizationName", post::getOrganizationName, post::setOrganizationName, postDTO.getOrganizationName());
        resourcePatchService.patchLocation(post, postDTO.getLocation());
        
        Optional<String> applyWebsiteOptional = postDTO.getApplyWebsite();
        Optional<DocumentDTO> applyDocumentOptional = postDTO.getApplyDocument();
        Optional<String> applyEmailOptional = postDTO.getApplyEmail();
    
        int applyNullCount = 0;
        int applyPresentCount = 0;
        for (Optional<?> applyOption : new Optional<?>[]{applyWebsiteOptional, applyDocumentOptional, applyEmailOptional}) {
            if (applyOption == null) {
                applyNullCount++;
            } else if (applyOption.isPresent()) {
                applyPresentCount++;
            }
        }
    
        if (applyNullCount < 3) {
            if (applyPresentCount == 0) {
                throw new ApiException(ExceptionCode.MISSING_POST_APPLY);
            } else if (applyPresentCount > 1) {
                throw new ApiException(ExceptionCode.CORRUPTED_POST_APPLY);
            }
        }
    
        resourcePatchService.patchProperty(post, "applyWebsite", post::getApplyWebsite, post::setApplyWebsite, applyWebsiteOptional, () -> {
            resourcePatchService.patchDocument(post, "applyDocument", post::getApplyDocument, post::setApplyDocument, Optional.empty());
            resourcePatchService.patchProperty(post, "applyEmail", post::getApplyEmail, post::setApplyEmail, Optional.empty());
        });
    
        resourcePatchService.patchDocument(post, "applyDocument", post::getApplyDocument, post::setApplyDocument, applyDocumentOptional, () -> {
            resourcePatchService.patchProperty(post, "applyWebsite", post::getApplyWebsite, post::setApplyWebsite, Optional.empty());
            resourcePatchService.patchProperty(post, "applyEmail", post::getApplyEmail, post::setApplyEmail, Optional.empty());
        });
    
        resourcePatchService.patchProperty(post, "applyEmail", post::getApplyEmail, post::setApplyEmail, applyEmailOptional, () -> {
            resourcePatchService.patchDocument(post, "applyDocument", post::getApplyDocument, post::setApplyDocument, Optional.empty());
            resourcePatchService.patchProperty(post, "applyWebsite", post::getApplyWebsite, post::setApplyWebsite, Optional.empty());
        });
        
        Board board = (Board) post.getParent();
        Department department = (Department) board.getParent();
        patchCategories(post, CategoryType.POST, postDTO.getPostCategories(), board);
        patchCategories(post, CategoryType.MEMBER, postDTO.getMemberCategories(), department);
    
        resourcePatchService.patchProperty(post, "existingRelation", post::getExistingRelation, post::setExistingRelation, postDTO.getExistingRelation());
        patchExistingRelationExplanation(post, postDTO.getExistingRelationExplanation());
        
        Optional<LocalDateTime> liveTimestampOptional = postDTO.getLiveTimestamp();
        Optional<LocalDateTime> deadTimestampOptional = postDTO.getDeadTimestamp();
        if (liveTimestampOptional != null && deadTimestampOptional != null) {
            if (liveTimestampOptional.isPresent() && deadTimestampOptional.isPresent()) {
                resourcePatchService.patchProperty(post, "liveTimestamp", post::getLiveTimestamp, post::setLiveTimestamp,
                    Optional.of(liveTimestampOptional.get().truncatedTo(ChronoUnit.SECONDS)));
                resourcePatchService.patchProperty(post, "deadTimestamp", post::getDeadTimestamp, post::setDeadTimestamp,
                    Optional.of(deadTimestampOptional.get().truncatedTo(ChronoUnit.SECONDS)));
            } else {
                LocalDateTime baseline = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
                resourcePatchService.patchProperty(post, "liveTimestamp", post::getLiveTimestamp, post::setLiveTimestamp, Optional.of(baseline));
                resourcePatchService.patchProperty(post, "deadTimestamp", post::getDeadTimestamp, post::setDeadTimestamp, Optional.of(baseline.plusWeeks(4)));
            }
        }
    }
    
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private void patchCategories(Post post, CategoryType categoryType, Optional<List<String>> categories, Resource reference) {
        if (categories != null) {
            List<String> oldCategories = resourceService.getCategories(post, categoryType);
            if (categories.isPresent()) {
                List<String> newCategories = new ArrayList<>(categories.get());
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
    
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private void patchExistingRelationExplanation(Post post, Optional<LinkedHashMap<String, Object>> existingRelationExplanation) {
        if (existingRelationExplanation != null) {
            String newValue = null;
            LinkedHashMap<String, Object> newValueMap = existingRelationExplanation.orElse(null);
            if (newValueMap != null) {
                newValue = mapExistingRelationExplanation(newValueMap);
            }
            
            String oldValue = post.getExistingRelationExplanation();
            if (!Objects.equals(oldValue, newValue)) {
                post.setExistingRelationExplanation(newValue);
                LinkedHashMap<String, Object> oldValueMap = oldValue == null ? null : mapExistingRelationExplanation(oldValue);
                post.getChangeList().put("existingRelationExplanation", oldValueMap, newValueMap);
            }
        }
    }
    
    private void updateCategories(Post post, CategoryType type, List<String> categories, Resource reference) {
        // Validate the update
        if (type == CategoryType.POST) {
            validateCategories(reference, type, categories,
                ExceptionCode.MISSING_POST_POST_CATEGORIES,
                ExceptionCode.CORRUPTED_POST_POST_CATEGORIES,
                ExceptionCode.INVALID_POST_POST_CATEGORIES);
        } else {
            validateCategories(reference, type, categories,
                ExceptionCode.MISSING_POST_MEMBER_CATEGORIES,
                ExceptionCode.CORRUPTED_POST_MEMBER_CATEGORIES,
                ExceptionCode.INVALID_POST_MEMBER_CATEGORIES);
        }
        
        // Clear the old records
        resourceService.deleteResourceCategories(post, type);
        Set<ResourceCategory> oldCategories = post.getCategories();
        oldCategories.removeIf(next -> next.getType() == type);
        
        // Index the insertion order
        Map<String, Integer> orderIndex = BoardUtils.getOrderIndex(categories);
        if (orderIndex != null) {
            // Reorder the old records
            List<ResourceCategory> resourceCategories = post.getCategories(type);
            if (resourceCategories != null) {
                resourceCategories.forEach(resourceCategory -> {
                    Integer ordinal = orderIndex.get(resourceCategory.getName());
                    resourceCategory.setOrdinal(ordinal == null ? null : ordinal);
                });
            }
            
            // Write the new records
            categories.forEach(category -> {
                ResourceCategory resourceCategory = new ResourceCategory().setResource(post).setName(category).setOrdinal(orderIndex.get(category)).setType(type);
                resourceCategory = resourceService.createResourceCategory(resourceCategory);
                oldCategories.add(resourceCategory);
            });
        }
    }
    
    @SuppressWarnings("JpaQlInspection")
    private void executeActions(List<Long> postIds, Action action, State state, LocalDateTime baseline) {
        if (postIds.size() > 0) {
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
                .setParameter("action", action.name())
                .setParameter("baseline", baseline)
                .setParameter("postIds", postIds)
                .executeUpdate();
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
    
    private void validateCategories(Resource reference, CategoryType type, List<String> categories, ExceptionCode missing, ExceptionCode invalid, ExceptionCode corrupted) {
        List<ResourceCategory> referenceCategories = reference.getCategories(type);
        if (referenceCategories != null) {
            if (CollectionUtils.isEmpty(categories)) {
                throw new ApiException(missing);
            } else if (!referenceCategories.stream().map(ResourceCategory::getName).collect(Collectors.toList()).containsAll(categories)) {
                throw new ApiException(invalid);
            }
        } else if (CollectionUtils.isNotEmpty(categories)) {
            throw new ApiException(corrupted);
        }
    }
    
}
