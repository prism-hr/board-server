package hr.prism.board.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hr.prism.board.domain.*;
import hr.prism.board.domain.ResourceEvent;
import hr.prism.board.dto.*;
import hr.prism.board.enums.*;
import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.repository.PostRepository;
import hr.prism.board.representation.ChangeListRepresentation;
import hr.prism.board.service.cache.UserRoleCacheService;
import hr.prism.board.service.event.ActivityEventService;
import hr.prism.board.service.event.NotificationEventService;
import hr.prism.board.util.BoardUtils;
import hr.prism.board.workflow.Activity;
import hr.prism.board.workflow.Notification;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional
public class PostService {

    private static final String SIMILAR_ORGANIZATION =
        "SELECT resource.organization_name, " +
            "IF(resource.scope = :scope, 1, 0) AS valid, " +
            "IF(resource.organization_name LIKE :searchTermHard, 1, 0) AS similarityHard, " +
            "MATCH resource.organization_name against(:searchTermSoft IN BOOLEAN MODE) AS similaritySoft " +
            "FROM resource " +
            "GROUP BY resource.organization_name " +
            "HAVING valid = 1 AND (similarityHard = 1 OR similaritySoft > 0) " +
            "ORDER BY similarityHard DESC, similaritySoft DESC, resource.organization_name " +
            "LIMIT 10";

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
    private UserRoleCacheService userRoleCacheService;

    @Inject
    private UserService userService;

    @Inject
    private ActionService actionService;

    @Inject
    private ResourceEventService resourceEventService;

    @Lazy
    @Inject
    private ActivityEventService activityEventService;

    @Lazy
    @Inject
    private NotificationEventService notificationEventService;

    @Inject
    private ObjectMapper objectMapper;

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    @SuppressWarnings("SpringJavaAutowiringInspection")
    private PlatformTransactionManager platformTransactionManager;

    public Post getPost(Long id) {
        return getPost(id, null);
    }

    public Post getPost(Long id, String ipAddress) {
        User user = userService.getCurrentUser();
        Post post = (Post) resourceService.getResource(user, Scope.POST, id);
        actionService.executeAction(user, post, Action.VIEW, () -> post);
        resourceEventService.getOrCreatePostView(post, user, ipAddress);

        if (user != null && !postRepository.findPostIdsRespondedTo(Collections.singletonList(post.getId()), user).isEmpty()) {
            post.setResponded(true);
        }

        return post;
    }

    public Post getPostApply(Long id, String ipAddress) {
        User user = userService.getCurrentUser();
        Post post = (Post) resourceService.getResource(user, Scope.POST, id);
        return (Post) actionService.executeAction(user, post, Action.PURSUE, () -> {
            resourceEventService.getOrCreatePostReferral(post, user, ipAddress);
            return post;
        });
    }

    public List<Post> getByName(String name) {
        return postRepository.findByName(name);
    }

    public List<Post> getPosts(Long boardId, Boolean includePublicPosts) {
        User user = userService.getCurrentUser();
        List<Post> posts =
            resourceService.getResources(user,
                new ResourceFilterDTO()
                    .setScope(Scope.POST)
                    .setParentId(boardId)
                    .setIncludePublicResources(includePublicPosts)
                    .setOrderStatement("order by resource.updatedTimestamp desc"))
                .stream().map(resource -> (Post) resource).collect(Collectors.toList());

        if (user != null) {
            Map<Long, Post> postIndex = posts.stream().collect(Collectors.toMap(Post::getId, post -> post));
            postRepository.findPostIdsRespondedTo(postIndex.keySet(), user).forEach(postId -> {
                Post post = postIndex.get(postId);
                if (post != null) {
                    post.setResponded(true);
                }
            });
        }

        return posts;
    }

    public Post createPost(Long boardId, PostDTO postDTO) {
        User currentUser = userService.getCurrentUserSecured();
        Board board = (Board) resourceService.getResource(currentUser, Scope.BOARD, boardId);
        Post createdPost = (Post) actionService.executeAction(currentUser, board, Action.EXTEND, () -> {
            Post post = new Post();
            Department department = (Department) board.getParent();

            post.setName(postDTO.getName());
            post.setSummary(postDTO.getSummary());
            post.setDescription(postDTO.getDescription());
            post.setOrganizationName(postDTO.getOrganizationName());
            post.setExistingRelation(postDTO.getExistingRelation());
            post.setExistingRelationExplanation(mapExistingRelationExplanation(postDTO.getExistingRelationExplanation()));
            post.setApplyWebsite(postDTO.getApplyWebsite());
            post.setApplyEmail(postDTO.getApplyEmail());
            post.setForwardCandidates(postDTO.getForwardCandidates());

            if (postDTO.getApplyDocument() != null) {
                post.setApplyDocument(documentService.getOrCreateDocument(postDTO.getApplyDocument()));
            }

            validatePostApply(post);
            post.setLocation(locationService.getOrCreateLocation(postDTO.getLocation()));

            LocalDateTime liveTimestamp = postDTO.getLiveTimestamp();
            LocalDateTime deadTimestamp = postDTO.getDeadTimestamp();
            post.setLiveTimestamp(liveTimestamp == null ? null : liveTimestamp.truncatedTo(ChronoUnit.SECONDS));
            post.setDeadTimestamp(deadTimestamp == null ? null : deadTimestamp.truncatedTo(ChronoUnit.SECONDS));

            post = postRepository.save(post);
            updateCategories(post, CategoryType.POST, postDTO.getPostCategories(), board);
            updateCategories(post, CategoryType.MEMBER, MemberCategory.toStrings(postDTO.getMemberCategories()), department);
            resourceService.createResourceRelation(board, post);
            userRoleService.createOrUpdateUserRole(post, currentUser, Role.ADMINISTRATOR);
            return post;
        });

        if (createdPost.getState() == State.DRAFT && createdPost.getExistingRelation() == null) {
            throw new BoardException(ExceptionCode.MISSING_POST_EXISTING_RELATION);
        }

        return createdPost;
    }

    public Post executeAction(Long id, Action action, PostPatchDTO postDTO) {
        User currentUser = userService.getCurrentUserSecured();
        Post post = (Post) resourceService.getResource(currentUser, Scope.POST, id);
        post.setComment(postDTO.getComment());
        return (Post) actionService.executeAction(currentUser, post, action, () -> {
            if (action == Action.EDIT) {
                updatePost(post, postDTO);
            } else {
                if (action == Action.ACCEPT) {
                    Resource board = post.getParent();
                    userService.findByRoleWithoutRole(post, Role.ADMINISTRATOR, board, Role.AUTHOR)
                        .forEach(user -> userRoleCacheService.createUserRole(currentUser, board, user, new UserRoleDTO(Role.AUTHOR), false));
                }

                if (BoardUtils.hasUpdates(postDTO)) {
                    actionService.executeAction(currentUser, post, Action.EDIT, () -> {
                        updatePost(post, postDTO);
                        return post;
                    });
                }
            }

            return post;
        });
    }

    public ResourceEvent createPostResponse(Long postId, ResourceEventDTO resourceEvent) {
        Post post = getPost(postId);
        User user = userService.getCurrentUserSecured();
        actionService.executeAction(user, post, Action.PURSUE, () -> post);
        return resourceEventService.getOrCreatePostResponse(post, user, resourceEvent);
    }

    public List<ResourceEvent> getPostResponses(Long postId) {
        Post post = getPost(postId);
        User user = userService.getCurrentUserSecured();
        actionService.executeAction(user, post, Action.AUDIT, () -> post);
        return resourceEventService.getResourceEvents(post, hr.prism.board.enums.ResourceEvent.RESPONSE);
    }

    public ResourceEvent getPostResponse(Long postId, Long responseId) {
        Post post = getPost(postId);
        User user = userService.getCurrentUserSecured();
        ResourceEvent resourceEvent = resourceEventService.findOne(responseId);
        if (resourceEvent.getUser().equals(user)) {
            return resourceEvent;
        }

        actionService.executeAction(user, post, Action.AUDIT, () -> post);
        return resourceEvent;
    }

    @Scheduled(initialDelay = 60000, fixedRate = 60000)
    public void publishAndRetirePostsScheduled() {
        publishAndRetirePosts();
    }

    public synchronized void publishAndRetirePosts() {
        LocalDateTime baseline = LocalDateTime.now();
        List<Long> postToRetireIds = postRepository.findPostsToRetire(Arrays.asList(State.PENDING, State.ACCEPTED), baseline);
        executeActions(postToRetireIds, Action.RETIRE, State.EXPIRED, baseline);
        List<Long> postToPublishIds = postRepository.findPostsToPublish(Arrays.asList(State.PENDING, State.EXPIRED), baseline);
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
            throw new BoardException(ExceptionCode.CORRUPTED_POST_EXISTING_RELATION_EXPLANATION, e);
        }
    }

    public LocalDateTime getEffectiveLiveTimestamp(Post post) {
        LocalDateTime baseline = LocalDateTime.now();
        LocalDateTime liveTimestamp = post.getLiveTimestamp();
        return liveTimestamp.isBefore(baseline) ? baseline : liveTimestamp;
    }

    public List<String> findOrganizationsBySimilarName(String searchTerm) {
        List<Object[]> rows = new TransactionTemplate(platformTransactionManager).execute(status ->
            entityManager.createNativeQuery(SIMILAR_ORGANIZATION)
                .setParameter("searchTermHard", searchTerm + "%")
                .setParameter("searchTermSoft", searchTerm)
                .setParameter("scope", Scope.POST.name())
                .getResultList());
        return rows.stream().map(row -> row[0].toString()).collect(Collectors.toList());
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private void updatePost(Post post, PostPatchDTO postDTO) {
        post.setChangeList(new ChangeListRepresentation());
        resourcePatchService.patchProperty(post, "name", post::getName, post::setName, postDTO.getName());
        resourcePatchService.patchProperty(post, "summary", post::getSummary, post::setSummary, postDTO.getSummary());
        resourcePatchService.patchProperty(post, "description", post::getDescription, post::setDescription, postDTO.getDescription());
        resourcePatchService.patchProperty(post, "organizationName", post::getOrganizationName, post::setOrganizationName, postDTO.getOrganizationName());
        resourcePatchService.patchLocation(post, postDTO.getLocation());

        resourcePatchService.patchProperty(post, "existingRelation", post::getExistingRelation, post::setExistingRelation, postDTO.getExistingRelation());
        patchExistingRelationExplanation(post, postDTO.getExistingRelationExplanation());

        Board board = (Board) post.getParent();
        Department department = (Department) board.getParent();
        patchCategories(post, CategoryType.POST, postDTO.getPostCategories(), board);
        patchCategories(post, CategoryType.MEMBER, MemberCategory.toStrings(postDTO.getMemberCategories()), department);

        Optional<String> applyWebsite = postDTO.getApplyWebsite();
        if (BoardUtils.isPresent(applyWebsite)) {
            patchPostApply(post, applyWebsite, Optional.empty(), Optional.empty());
        }

        Optional<DocumentDTO> applyDocument = postDTO.getApplyDocument();
        if (BoardUtils.isPresent(applyDocument)) {
            patchPostApply(post, Optional.empty(), applyDocument, Optional.empty());
        }

        Optional<String> applyEmail = postDTO.getApplyEmail();
        if (BoardUtils.isPresent(applyEmail)) {
            patchPostApply(post, Optional.empty(), Optional.empty(), applyEmail);
        }

        resourcePatchService.patchProperty(post, "forwardCandidates", post::getForwardCandidates, post::setForwardCandidates, postDTO.getForwardCandidates());

        Optional<LocalDateTime> liveTimestamp = postDTO.getLiveTimestamp();
        Optional<LocalDateTime> deadTimestamp = postDTO.getDeadTimestamp();
        resourcePatchService.patchProperty(post, "liveTimestamp", post::getLiveTimestamp, post::setLiveTimestamp,
            liveTimestamp != null ? liveTimestamp.map(t -> t.truncatedTo(ChronoUnit.SECONDS)) : null);
        resourcePatchService.patchProperty(post, "deadTimestamp", post::getDeadTimestamp, post::setDeadTimestamp,
            deadTimestamp != null ? deadTimestamp.map(t -> t.truncatedTo(ChronoUnit.SECONDS)) : null);

        postRepository.update(post);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private void patchPostApply(Post post, Optional<String> applyWebsite, Optional<DocumentDTO> applyDocument, Optional<String> applyEmail) {
        resourcePatchService.patchProperty(post, "applyWebsite", post::getApplyWebsite, post::setApplyWebsite, applyWebsite);
        resourcePatchService.patchDocument(post, "applyDocument", post::getApplyDocument, post::setApplyDocument, applyDocument);
        resourcePatchService.patchProperty(post, "applyEmail", post::getApplyEmail, post::setApplyEmail, applyEmail);
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
            resourceService.validateCategories(reference, type, categories,
                ExceptionCode.MISSING_POST_POST_CATEGORIES,
                ExceptionCode.INVALID_POST_POST_CATEGORIES,
                ExceptionCode.CORRUPTED_POST_POST_CATEGORIES);
        } else {
            resourceService.validateCategories(reference, type, categories,
                ExceptionCode.MISSING_POST_MEMBER_CATEGORIES,
                ExceptionCode.INVALID_POST_MEMBER_CATEGORIES,
                ExceptionCode.CORRUPTED_POST_MEMBER_CATEGORIES);
        }

        // Clear the old records
        resourceService.deleteResourceCategories(post, type);
        Set<ResourceCategory> oldCategories = post.getCategories();
        oldCategories.removeIf(next -> next.getType() == type);

        // Index the insertion order
        Map<String, Integer> orderIndex = BoardUtils.getOrderIndex(categories);
        if (orderIndex != null) {
            // Write the new records
            categories.forEach(category -> {
                ResourceCategory resourceCategory = new ResourceCategory().setResource(post).setName(category).setOrdinal(orderIndex.get(category)).setType(type);
                resourceCategory = resourceService.createResourceCategory(resourceCategory);
                oldCategories.add(resourceCategory);
            });
        }
    }

    @SuppressWarnings("JpaQlInspection")
    // TODO: Register the prompts
    private void executeActions(List<Long> postIds, Action action, State newState, LocalDateTime baseline) {
        if (!postIds.isEmpty()) {
            new TransactionTemplate(platformTransactionManager).execute(status -> {
                entityManager.createQuery(
                    "update Post post " +
                        "set post.previousState = post.state, " +
                        "post.state = :newState, " +
                        "post.updatedTimestamp = :baseline " +
                        "where post.id in (:postIds)")
                    .setParameter("newState", newState)
                    .setParameter("baseline", baseline)
                    .setParameter("postIds", postIds)
                    .executeUpdate();

                //noinspection SqlResolve
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
                return null;
            });

            for (Long postId : postIds) {
                List<Activity> activities = new ArrayList<>();
                List<Notification> notifications = new ArrayList<>();
                if (action == Action.PUBLISH) {
                    activities.add(new Activity().setScope(Scope.POST).setRole(Role.ADMINISTRATOR).setActivity(hr.prism.board.enums.Activity.PUBLISH_POST_ACTIVITY));
                    activities.add(new Activity().setScope(Scope.DEPARTMENT).setRole(Role.MEMBER).setActivity(hr.prism.board.enums.Activity.PUBLISH_POST_MEMBER_ACTIVITY));
                    notifications.add(new Notification().setScope(Scope.POST).setRole(Role.ADMINISTRATOR).setNotification(hr.prism.board.enums.Notification.PUBLISH_POST_NOTIFICATION));
                    notifications.add(new Notification().setScope(Scope.DEPARTMENT).setRole(Role.MEMBER).setNotification(hr.prism.board.enums.Notification.PUBLISH_POST_MEMBER_NOTIFICATION));
                } else {
                    activities.add(new Activity().setScope(Scope.POST).setRole(Role.ADMINISTRATOR).setActivity(hr.prism.board.enums.Activity.RETIRE_POST_ACTIVITY));
                    notifications.add(new Notification().setScope(Scope.POST).setRole(Role.ADMINISTRATOR).setNotification(hr.prism.board.enums.Notification.RETIRE_POST_NOTIFICATION));
                }

                activityEventService.publishEvent(this, postId, activities);
                notificationEventService.publishEvent(this, postId, notifications);
            }
        }
    }

    private String mapExistingRelationExplanation(LinkedHashMap<String, Object> existingRelationExplanation) {
        if (existingRelationExplanation == null) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(existingRelationExplanation);
        } catch (JsonProcessingException e) {
            throw new BoardException(ExceptionCode.CORRUPTED_POST_EXISTING_RELATION_EXPLANATION, e);
        }
    }

    private void validatePostApply(Post post) {
        long applyCount = Stream.of(post.getApplyWebsite(), post.getApplyDocument(), post.getApplyEmail()).filter(Objects::nonNull).count();
        if (applyCount == 0) {
            throw new BoardException(ExceptionCode.MISSING_POST_APPLY);
        } else if (applyCount > 1) {
            throw new BoardException(ExceptionCode.CORRUPTED_POST_APPLY);
        }
    }

}
