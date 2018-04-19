package hr.prism.board.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import hr.prism.board.domain.*;
import hr.prism.board.domain.ResourceEvent;
import hr.prism.board.dto.*;
import hr.prism.board.enums.*;
import hr.prism.board.enums.ResourceTask;
import hr.prism.board.event.ActivityEvent;
import hr.prism.board.event.EventProducer;
import hr.prism.board.event.NotificationEvent;
import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.BoardForbiddenException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.repository.PostRepository;
import hr.prism.board.representation.ChangeListRepresentation;
import hr.prism.board.representation.DemographicDataStatusRepresentation;
import hr.prism.board.utils.BoardUtils;
import hr.prism.board.value.PostStatistics;
import hr.prism.board.workflow.Activity;
import hr.prism.board.workflow.Notification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static hr.prism.board.enums.Action.*;
import static hr.prism.board.enums.CategoryType.MEMBER;
import static hr.prism.board.enums.MemberCategory.toStrings;
import static hr.prism.board.enums.Role.ADMINISTRATOR;
import static hr.prism.board.enums.Scope.BOARD;
import static hr.prism.board.enums.State.ARCHIVED;
import static hr.prism.board.exception.ExceptionCode.FORBIDDEN_REFERRAL;
import static hr.prism.board.utils.ResourceUtils.makeResourceFilter;
import static hr.prism.board.utils.ResourceUtils.validateCategories;

import hr.prism.board.event.ActivityEvent;

@Service
@Transactional
public class PostService {

    private static final List<ResourceTask> POST_TASKS = ImmutableList.of(ResourceTask.CREATE_POST);

    @Inject
    private PostRepository postRepository;

    @Inject
    private DocumentService documentService;

    @Inject
    private LocationService locationService;

    @Inject
    private OrganizationService organizationService;

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

    @Inject
    private ActivityService activityService;

    @Inject
    private DepartmentService departmentService;

    @Inject
    private DepartmentUserService departmentUserService;

    @Inject
    private ResourceTaskService resourceTaskService;

    @Inject
    private ObjectMapper objectMapper;

    @Inject
    private EntityManager entityManager;

    @Inject
    private EventProducer eventProducer;

    public Post getPost(Long id) {
        return getPost(id, null, false);
    }

    public Post getPost(Long id, String ipAddress, boolean recordView) {
        User user = userService.getCurrentUser();
        Post post = (Post) resourceService.getResource(user, Scope.POST, id);
        actionService.executeAction(user, post, VIEW, () -> post);

        if (recordView) {
            resourceEventService.createPostView(post, user, ipAddress);
            if (user != null) {
                addPostResponseReadiness(post, user);
            }
        }

        addPostResponse(post, user);
        return post;
    }

    public List<Post> getByName(String name) {
        return postRepository.findByName(name);
    }

    public List<Post> getPosts(Long boardId, Boolean includePublicPosts, State state, String quarter, String searchTerm) {
        User user = userService.getCurrentUser();
        List<Post> posts =
            resourceService.getResources(user,
                makeResourceFilter(Scope.POST, boardId, includePublicPosts, state, quarter, searchTerm)
                    .setOrderStatement("resource.updatedTimestamp DESC, resource.id DESC"))
                .stream().map(resource -> (Post) resource).collect(Collectors.toList());

        if (posts.isEmpty()) {
            return posts;
        }

        decoratePosts(user, posts);
        return posts;
    }

    public List<ResourceOperation> getPostOperations(Long id) {
        User user = userService.getCurrentUserSecured();
        Post post = (Post) resourceService.getResource(user, Scope.POST, id);
        actionService.executeAction(user, post, EDIT, () -> post);
        return resourceService.getResourceOperations(post);
    }

    public List<String> getPostArchiveQuarters(Long parentId) {
        User user = userService.getCurrentUserSecured();
        return resourceService.getResourceArchiveQuarters(user, Scope.POST, parentId);
    }

    public Post createPost(Long boardId, PostDTO postDTO) {
        User user = userService.getCurrentUserSecured();
        Board board = (Board) resourceService.getResource(user, BOARD, boardId);
        Post createdPost = (Post) actionService.executeAction(user, board, EXTEND, () -> {
            Post post = new Post();
            Department department = (Department) board.getParent();

            post.setName(postDTO.getName());
            post.setSummary(postDTO.getSummary());
            post.setDescription(postDTO.getDescription());

            Organization organization = organizationService.getOrCreateOrganization(postDTO.getOrganization());
            post.setOrganization(organization);
            user.setDefaultOrganization(organization);

            Location location = locationService.getOrCreateLocation(postDTO.getLocation());
            post.setLocation(location);
            user.setDefaultLocation(location);

            post.setExistingRelation(postDTO.getExistingRelation());
            post.setExistingRelationExplanation(
                mapExistingRelationExplanation(postDTO.getExistingRelationExplanation()));

            String applyWebsite = postDTO.getApplyWebsite();
            if (applyWebsite != null) {
                verifyApplyWebsiteAccessible(applyWebsite);
                post.setApplyWebsite(applyWebsite);
            }

            post.setApplyEmail(postDTO.getApplyEmail());

            DocumentDTO applyDocument = postDTO.getApplyDocument();
            if (applyDocument != null) {
                post.setApplyDocument(documentService.getOrCreateDocument(applyDocument));
            }

            validatePostApply(post);

            LocalDateTime liveTimestamp = postDTO.getLiveTimestamp();
            LocalDateTime deadTimestamp = postDTO.getDeadTimestamp();
            post.setLiveTimestamp(liveTimestamp);
            post.setDeadTimestamp(deadTimestamp);
            post = postRepository.save(post);

            updateCategories(post, CategoryType.POST, postDTO.getPostCategories(), board);
            updateCategories(post, MEMBER, toStrings(postDTO.getMemberCategories()), department);
            resourceService.createResourceRelation(board, post);
            setIndexDataAndQuarter(post);
            userRoleService.createOrUpdateUserRole(post, user, ADMINISTRATOR);
            resourceTaskService.completeTasks(department, POST_TASKS);
            return post;
        });

        addPostResponseReadiness(createdPost, user);
        addPostResponse(createdPost, user);
        if (createdPost.getState() == State.DRAFT && createdPost.getExistingRelation() == null) {
            throw new BoardException(ExceptionCode.MISSING_POST_EXISTING_RELATION, "Existing relation explanation required");
        }

        return createdPost;
    }

    public Post executeAction(Long id, Action action, PostPatchDTO postDTO) {
        User user = userService.getCurrentUserSecured();
        Post post = (Post) resourceService.getResource(user, Scope.POST, id);
        post.setComment(postDTO.getComment());
        return (Post) actionService.executeAction(user, post, action, () -> {
            if (action == Action.EDIT) {
                updatePost(post, postDTO);
            } else {
                if (action == Action.ACCEPT) {
                    Resource department = post.getParent().getParent();
                    userService.findByRoleWithoutRole(post, ADMINISTRATOR, department, Role.AUTHOR)
                        .forEach(author -> userRoleCacheService.createUserRole(author, department, author, new UserRoleDTO(Role.AUTHOR), false));
                }

                if (BoardUtils.hasUpdates(postDTO)) {
                    actionService.executeAction(user, post, Action.EDIT, () -> {
                        updatePost(post, postDTO);
                        return post;
                    });
                }
            }

            addPostResponseReadiness(post, user);
            addPostResponse(post, user);
            return post;
        });
    }

    public String getPostReferral(String referral) {
        ResourceEvent resourceEvent = resourceEventService.getAndConsumeReferral(referral);
        Post post = (Post) resourceEvent.getResource();
        departmentUserService.checkValidDemographicData(resourceEvent.getUser(), (Department) post.getParent()
            .getParent(), BoardForbiddenException.class, FORBIDDEN_REFERRAL);

        Document applyDocument = post.getApplyDocument();
        String redirect = applyDocument == null ? post.getApplyWebsite() : applyDocument.getCloudinaryUrl();
        if (post.getState() != State.ACCEPTED || redirect == null) {
            // We may no longer be redirecting - throw an exception so client can refresh
            throw new BoardException(ExceptionCode.INVALID_REFERRAL, "Post no longer accepting referrals");
        }

        return redirect;
    }

    public ResourceEvent createPostResponse(Long postId, ResourceEventDTO resourceEvent) {
        Post post = getPost(postId);
        User user = userService.getCurrentUserSecured(true);
        actionService.executeAction(user, post, Action.PURSUE, () -> {
            departmentUserService.checkValidDemographicData(user, (Department) post.getParent()
                .getParent(), BoardForbiddenException.class, ExceptionCode.FORBIDDEN_RESPONSE);
            return post;
        });

        actionService.executeAction(user, post, Action.PURSUE, () -> post);
        return resourceEventService.getOrCreatePostResponse(post, user, resourceEvent).setExposeResponseData(true);
    }

    @SuppressWarnings("JpaQlInspection")
    public Collection<ResourceEvent> getPostResponses(Long postId, String searchTerm) {
        Post post = getPost(postId);
        User user = userService.getCurrentUserSecured();
        actionService.executeAction(user, post, Action.EDIT, () -> post);

        List<Long> userIds;
        boolean targetingReferrals = post.getApplyEmail() == null;
        if (targetingReferrals) {
            userIds = userService.findByResourceAndEvents(post,
                Arrays.asList(hr.prism.board.enums.ResourceEvent.REFERRAL, hr.prism.board.enums.ResourceEvent.RESPONSE));
        } else {
            userIds = userService.findByResourceAndEvent(post, hr.prism.board.enums.ResourceEvent.RESPONSE);
        }

        if (userIds.isEmpty()) {
            return Collections.emptyList();
        }

        String search = UUID.randomUUID().toString();
        boolean searchTermApplied = searchTerm != null;
        if (searchTermApplied) {
            resourceEventService.createSearchResults(search, searchTerm, userIds);
            entityManager.flush();
        }

        String statement =
            "select distinct resourceEvent " +
                "from ResourceEvent resourceEvent " +
                "left join resourceEvent.searches search on search.search = :search " +
                "where resourceEvent.resource.id = :postId " +
                "and resourceEvent.user.id in (:userIds) ";
        if (searchTermApplied) {
            statement += "and search.id is not null ";
        }

        statement += "order by search.id, resourceEvent.id desc";
        List<ResourceEvent> resourceEvents = entityManager.createQuery(statement, ResourceEvent.class)
            .setParameter("search", search)
            .setParameter("postId", postId)
            .setParameter("userIds", userIds)
            .getResultList();

        if (searchTermApplied) {
            resourceEventService.deleteSearchResults(search);
        }

        if (resourceEvents.isEmpty()) {
            return Collections.emptyList();
        }

        HashMultimap<String, User> userIpAddresses = HashMultimap.create();
        Map<User, ResourceEvent> userResourceEvents = new LinkedHashMap<>();

        resourceEvents.forEach(resourceEvent -> {
            User resourceEventUser = resourceEvent.getUser();
            ResourceEvent headResourceEvent = userResourceEvents.get(resourceEventUser);
            if (headResourceEvent == null) {
                userResourceEvents.put(resourceEventUser, resourceEvent);
            } else if (resourceEvent.getEvent() == hr.prism.board.enums.ResourceEvent.RESPONSE || resourceEvent.hasDemographicData()) {
                userResourceEvents.put(resourceEventUser, resourceEvent);
                List<ResourceEvent> resourceEventHistory = new ArrayList<>();
                resourceEventHistory.add(headResourceEvent);

                List<ResourceEvent> previousResourceEventHistory = headResourceEvent.getHistory();
                if (previousResourceEventHistory != null) {
                    resourceEventHistory.addAll(previousResourceEventHistory);
                }

                resourceEvent.setHistory(resourceEventHistory);
            } else {
                appendToResourceEventHistory(headResourceEvent, resourceEvent);
            }

            String ipAddress = resourceEvent.getIpAddress();
            if (ipAddress != null) {
                userIpAddresses.put(ipAddress, resourceEventUser);
            }
        });

        if (!userIpAddresses.isEmpty()) {
            resourceEventService.findByIpAddresses(userIpAddresses.keySet()).forEach(resourceEvent ->
                userIpAddresses.get(resourceEvent.getIpAddress()).forEach(resourceEventUser ->
                    appendToResourceEventHistory(userResourceEvents.get(resourceEventUser), resourceEvent)));
        }

        Collection<ResourceEvent> headResourceEvents = userResourceEvents.values();
        Map<hr.prism.board.domain.Activity, ResourceEvent> indexByActivities = new HashMap<>();
        for (ResourceEvent headResourceEvent : headResourceEvents) {
            headResourceEvent.setExposeResponseData(headResourceEvent.getUser().equals(user));

            hr.prism.board.domain.Activity activity = headResourceEvent.getActivity();
            if (activity != null) {
                indexByActivities.put(activity, headResourceEvent);
            }
        }

        if (!indexByActivities.isEmpty()) {
            for (hr.prism.board.domain.ActivityEvent activityEvent : activityService.findViews(indexByActivities.keySet(), user)) {
                indexByActivities.get(activityEvent.getActivity()).setViewed(true);
            }
        }

        return headResourceEvents;
    }

    public ResourceEvent getPostResponse(Long postId, Long responseId) {
        return getPostResponse(userService.getCurrentUserSecured(), postId, responseId);
    }

    public ResourceEvent putPostResponseView(Long postId, Long responseId) {
        User user = userService.getCurrentUserSecured();
        ResourceEvent resourceEvent = getPostResponse(user, postId, responseId);
        activityService.viewActivity(resourceEvent.getActivity(), user);
        return resourceEvent.setViewed(true);
    }

    public void publishAndRetirePosts(LocalDateTime baseline) {
        List<Long> postToRetireIds = postRepository.findPostsToRetire(Arrays.asList(State.PENDING, State.ACCEPTED), baseline);
        executeActions(postToRetireIds, Action.RETIRE, State.EXPIRED, baseline);
        List<Long> postToPublishIds = postRepository.findPostsToPublish(Arrays.asList(State.PENDING, State.EXPIRED), State.REJECTED, baseline);
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
            throw new BoardException(ExceptionCode.CORRUPTED_POST_EXISTING_RELATION_EXPLANATION, "Unable to deserialize existing relation explanation", e);
        }
    }

    public LocalDateTime getEffectiveLiveTimestamp(Post post) {
        LocalDateTime baseline = LocalDateTime.now();
        LocalDateTime liveTimestamp = post.getLiveTimestamp();
        return liveTimestamp.isBefore(baseline) ? baseline : liveTimestamp;
    }

    public void setIndexDataAndQuarter(Post post) {
        resourceService.setIndexDataAndQuarter(post, post.getName(), post.getSummary(), post.getDescription(),
            post.getOrganization().getName(), post.getLocation().getName());
    }

    public Post findLatestPost(User user) {
        return postRepository.findLatestPost(user, ADMINISTRATOR, Scope.POST);
    }

    public List<Post> getPosts(Long boardId) {
        return getPosts(boardId, true, null, null, null);
    }

    public PostStatistics getPostStatistics(Long departmentId) {
        return (PostStatistics) entityManager.createNamedQuery("postStatistics")
            .setParameter("departmentId", departmentId)
            .getSingleResult();
    }

    @SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "OptionalAssignedToNull"})
    private void updatePost(Post post, PostPatchDTO postDTO) {
        post.setChangeList(new ChangeListRepresentation());
        resourcePatchService.patchProperty(post, "name", post::getName, post::setName, postDTO.getName());
        resourcePatchService.patchProperty(post, "summary", post::getSummary, post::setSummary, postDTO.getSummary());
        resourcePatchService.patchProperty(post, "description", post::getDescription, post::setDescription, postDTO.getDescription());
        resourcePatchService.patchProperty(post, "organizationName", post::getOrganizationName, post::setOrganizationName, postDTO.getOrganizationName());
        resourcePatchService.patchProperty(post, "organizationLogo", post::getOrganizationLogo, post::setOrganizationLogo, postDTO.getOrganizationLogo());
        resourcePatchService.patchLocation(post, postDTO.getLocation());

        Optional<String> applyWebsite = postDTO.getApplyWebsite();
        if (BoardUtils.isPresent(applyWebsite)) {
            verifyApplyWebsiteAccessible(applyWebsite.get());
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

        Board board = (Board) post.getParent();
        Department department = (Department) board.getParent();
        patchCategories(post, CategoryType.POST, postDTO.getPostCategories(), board);
        patchCategories(post, MEMBER, toStrings(postDTO.getMemberCategories()), department);

        resourcePatchService.patchProperty(post, "existingRelation", post::getExistingRelation, post::setExistingRelation, postDTO.getExistingRelation());
        patchExistingRelationExplanation(post, postDTO.getExistingRelationExplanation());

        Optional<LocalDateTime> liveTimestamp = postDTO.getLiveTimestamp();
        Optional<LocalDateTime> deadTimestamp = postDTO.getDeadTimestamp();
        resourcePatchService.patchProperty(post, "liveTimestamp", post::getLiveTimestamp, post::setLiveTimestamp,
            liveTimestamp != null ? liveTimestamp.map(t -> t.truncatedTo(ChronoUnit.SECONDS)) : null);
        resourcePatchService.patchProperty(post, "deadTimestamp", post::getDeadTimestamp, post::setDeadTimestamp,
            deadTimestamp != null ? deadTimestamp.map(t -> t.truncatedTo(ChronoUnit.SECONDS)) : null);

        setIndexDataAndQuarter(post);
        postRepository.update(post);
    }

    public void archivePosts() {
        LocalDateTime baseline = LocalDateTime.now();
        List<Long> postIds = resourceService.getResourcesToArchive(baseline);
        if (!postIds.isEmpty()) {
            actionService.executeAnonymously(postIds, ARCHIVE, ARCHIVED, baseline);
        }
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
                    post.getChangeList()
                        .put(categoryType.name().toLowerCase() + "Categories", oldCategories, resourceService.getCategories(post, categoryType));
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
                ExceptionCode.INVALID_POST_POST_CATEGORIES,
                ExceptionCode.CORRUPTED_POST_POST_CATEGORIES);
        } else {
            validateCategories(reference, type, categories,
                ExceptionCode.MISSING_POST_MEMBER_CATEGORIES,
                ExceptionCode.INVALID_POST_MEMBER_CATEGORIES,
                ExceptionCode.CORRUPTED_POST_MEMBER_CATEGORIES);
        }

        resourceService.updateCategories(post, type, categories);
    }

    private void executeActions(List<Long> postIds, Action action, State newState, LocalDateTime baseline) {
        if (!postIds.isEmpty()) {
            actionService.executeAnonymously(postIds, action, newState, baseline);
            for (Long postId : postIds) {
                List<Activity> activities = new ArrayList<>();
                List<Notification> notifications = new ArrayList<>();
                if (action == Action.PUBLISH) {
                    activities.add(new Activity().setScope(Scope.POST)
                        .setRole(ADMINISTRATOR)
                        .setActivity(hr.prism.board.enums.Activity.PUBLISH_POST_ACTIVITY));
                    activities.add(new Activity().setScope(Scope.DEPARTMENT)
                        .setRole(Role.MEMBER)
                        .setActivity(hr.prism.board.enums.Activity.PUBLISH_POST_MEMBER_ACTIVITY));
                    notifications.add(new Notification().setScope(Scope.POST)
                        .setRole(ADMINISTRATOR)
                        .setNotification(hr.prism.board.enums.Notification.PUBLISH_POST_NOTIFICATION));
                    notifications.add(new Notification().setScope(Scope.DEPARTMENT)
                        .setRole(Role.MEMBER)
                        .setNotification(hr.prism.board.enums.Notification.PUBLISH_POST_MEMBER_NOTIFICATION));
                } else {
                    activities.add(new Activity().setScope(Scope.POST)
                        .setRole(ADMINISTRATOR)
                        .setActivity(hr.prism.board.enums.Activity.RETIRE_POST_ACTIVITY));
                    notifications.add(new Notification().setScope(Scope.POST)
                        .setRole(ADMINISTRATOR)
                        .setNotification(hr.prism.board.enums.Notification.RETIRE_POST_NOTIFICATION));
                }

                eventProducer.produce(
                    new ActivityEvent(this, postId, true, activities),
                    new NotificationEvent(this, postId, notifications));
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
            throw new BoardException(ExceptionCode.CORRUPTED_POST_EXISTING_RELATION_EXPLANATION, "Unable to serialize existing relation explanation", e);
        }
    }

    private void validatePostApply(Post post) {
        long applyCount = Stream.of(post.getApplyWebsite(), post.getApplyDocument(), post.getApplyEmail()).filter(Objects::nonNull).count();
        if (applyCount == 0) {
            throw new BoardException(ExceptionCode.MISSING_POST_APPLY, "No apply mechanism specified");
        } else if (applyCount > 1) {
            throw new BoardException(ExceptionCode.CORRUPTED_POST_APPLY, "Multiple apply mechanisms specified");
        }
    }

    private ResourceEvent getPostResponse(User user, Long postId, Long responseId) {
        Post post = getPost(postId);
        actionService.executeAction(user, post, Action.EDIT, () -> post);
        ResourceEvent resourceEvent = resourceEventService.findOne(responseId);
        resourceEvent.setExposeResponseData(resourceEvent.getUser().equals(user));
        return resourceEvent;
    }

    private void addPostResponseReadiness(Post post, User user) {
        boolean canPursue = actionService.canExecuteAction(post, Action.PURSUE);
        DemographicDataStatusRepresentation responseReadiness =
            userRoleService.makeDemographicDataStatus(user, (Department) post.getParent().getParent(), canPursue);
        post.setResponseReadiness(responseReadiness);
        if (canPursue && responseReadiness.isReady() && post.getApplyEmail() == null) {
            resourceEventService.createPostReferral(post, user);
        }
    }

    private void addPostResponse(Post post, User user) {
        if (user != null) {
            entityManager.flush();
            post.setExposeApplyData(actionService.canExecuteAction(post, Action.EDIT));
            post.setReferral(resourceEventService.findByResourceAndEventAndUser(post, hr.prism.board.enums.ResourceEvent.REFERRAL, user));
            post.setResponse(resourceEventService.findByResourceAndEventAndUser(post, hr.prism.board.enums.ResourceEvent.RESPONSE, user));
        }
    }

    private void decoratePosts(User user, List<Post> posts) {
        if (user != null) {
            entityManager.flush();
            Map<Post, Post> postIndex = posts.stream().collect(Collectors.toMap(post -> post, post -> post));
            Map<Resource, ResourceEvent> referrals = resourceEventService.findByResourceIdsAndEventAndUser(posts, hr.prism.board.enums.ResourceEvent.REFERRAL, user)
                .stream().collect(Collectors.toMap(ResourceEvent::getResource, referral -> referral));
            Map<Resource, ResourceEvent> responses = resourceEventService.findByResourceIdsAndEventAndUser(posts, hr.prism.board.enums.ResourceEvent.RESPONSE, user)
                .stream().collect(Collectors.toMap(ResourceEvent::getResource, referral -> referral));

            for (Map.Entry<Post, Post> postIndexEntry : postIndex.entrySet()) {
                Post post = postIndexEntry.getValue();
                post.setExposeApplyData(actionService.canExecuteAction(post, Action.EDIT));
                post.setReferral(referrals.get(post));
                post.setResponse(responses.get(post));
            }
        }
    }

    private void appendToResourceEventHistory(ResourceEvent headResourceEvent, ResourceEvent resourceEvent) {
        List<ResourceEvent> resourceEventHistory = headResourceEvent.getHistory();
        if (resourceEventHistory == null) {
            resourceEventHistory = new ArrayList<>();
            headResourceEvent.setHistory(resourceEventHistory);
        }

        resourceEventHistory.add(resourceEvent);
    }

    private void verifyApplyWebsiteAccessible(String applyWebsite) {
        try {
            URL url = new URL(applyWebsite);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setInstanceFollowRedirects(true);
            connection.connect();

            int responseCode = connection.getResponseCode();
            if (responseCode < 200 || responseCode >= 400) {
                throw new BoardException(ExceptionCode.INACCESSIBLE_POST_APPLY, "Cannot access apply website");
            }
        } catch (IOException e) {
            throw new BoardException(ExceptionCode.INACCESSIBLE_POST_APPLY, "Cannot access apply website");
        }
    }

}
