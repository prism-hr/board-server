package hr.prism.board.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import hr.prism.board.dao.PostDAO;
import hr.prism.board.domain.*;
import hr.prism.board.domain.ResourceEvent;
import hr.prism.board.dto.DocumentDTO;
import hr.prism.board.dto.PostDTO;
import hr.prism.board.dto.PostPatchDTO;
import hr.prism.board.enums.*;
import hr.prism.board.event.ActivityEvent;
import hr.prism.board.event.EventProducer;
import hr.prism.board.event.NotificationEvent;
import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.repository.PostRepository;
import hr.prism.board.representation.ChangeListRepresentation;
import hr.prism.board.value.DemographicDataStatus;
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
import static hr.prism.board.enums.Activity.*;
import static hr.prism.board.enums.CategoryType.MEMBER;
import static hr.prism.board.enums.MemberCategory.toStrings;
import static hr.prism.board.enums.Notification.*;
import static hr.prism.board.enums.ResourceEvent.REFERRAL;
import static hr.prism.board.enums.ResourceEvent.RESPONSE;
import static hr.prism.board.enums.ResourceTask.POST_TASKS;
import static hr.prism.board.enums.Role.ADMINISTRATOR;
import static hr.prism.board.enums.Role.AUTHOR;
import static hr.prism.board.enums.Scope.*;
import static hr.prism.board.enums.State.*;
import static hr.prism.board.exception.ExceptionCode.*;
import static hr.prism.board.utils.BoardUtils.hasUpdates;
import static hr.prism.board.utils.BoardUtils.isPresent;
import static hr.prism.board.utils.ResourceUtils.makeResourceFilter;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

import hr.prism.board.event.ActivityEvent;

@Service
@Transactional
public class PostService {

    private final PostRepository postRepository;

    private final PostDAO postDAO;

    private final DocumentService documentService;

    private final LocationService locationService;

    private final OrganizationService organizationService;

    private final ResourceService resourceService;

    private final PostPatchService postPatchService;

    private final UserRoleService userRoleService;

    private final UserService userService;

    private final ActionService actionService;

    private final ResourceEventService resourceEventService;

    private final DepartmentUserService departmentUserService;

    private final ResourceTaskService resourceTaskService;

    private final EventProducer eventProducer;

    private final ObjectMapper objectMapper;

    private final EntityManager entityManager;

    @Inject
    public PostService(PostRepository postRepository, PostDAO postDAO, DocumentService documentService,
                       LocationService locationService, OrganizationService organizationService,
                       ResourceService resourceService, PostPatchService postPatchService,
                       UserRoleService userRoleService, UserService userService, ActionService actionService,
                       ResourceEventService resourceEventService, DepartmentUserService departmentUserService,
                       ResourceTaskService resourceTaskService, EventProducer eventProducer, ObjectMapper objectMapper,
                       EntityManager entityManager) {
        this.postRepository = postRepository;
        this.postDAO = postDAO;
        this.documentService = documentService;
        this.locationService = locationService;
        this.organizationService = organizationService;
        this.resourceService = resourceService;
        this.postPatchService = postPatchService;
        this.userRoleService = userRoleService;
        this.userService = userService;
        this.actionService = actionService;
        this.resourceEventService = resourceEventService;
        this.departmentUserService = departmentUserService;
        this.resourceTaskService = resourceTaskService;
        this.eventProducer = eventProducer;
        this.objectMapper = objectMapper;
        this.entityManager = entityManager;
    }

    public Post getById(Long id) {
        return getById(id, null, false);
    }

    public Post getById(Long id, String ipAddress, boolean recordView) {
        User user = userService.getUser();
        Post post = (Post) resourceService.getResource(user, POST, id);
        actionService.executeAction(user, post, VIEW, () -> post);

        if (recordView) {
            resourceEventService.createPostView(post, user, ipAddress);
        }

        addPostResponseReadiness(post, user);
        addPostResponse(post, user);
        return post;
    }

    public List<Post> getByName(String name) {
        return postRepository.findByName(name);
    }

    public List<Post> getPosts(Long boardId, Boolean includePublicPosts, State state, String quarter,
                               String searchTerm) {
        User user = userService.getUser();
        List<Post> posts =
            resourceService.getResources(user,
                makeResourceFilter(POST, boardId, includePublicPosts, state, quarter, searchTerm)
                    .setOrderStatement("resource.updatedTimestamp DESC, resource.id DESC"))
                .stream().map(resource -> (Post) resource).collect(Collectors.toList());

        if (posts.isEmpty()) {
            return posts;
        }

        addPostResponsesAndReadinesses(user, posts);
        return posts;
    }

    public List<ResourceOperation> getPostOperations(Long id) {
        User user = userService.getUserSecured();
        Post post = (Post) resourceService.getResource(user, POST, id);
        actionService.executeAction(user, post, EDIT, () -> post);
        return resourceService.getResourceOperations(post);
    }

    public List<String> getPostArchiveQuarters(Long parentId) {
        User user = userService.getUserSecured();
        return resourceService.getResourceArchiveQuarters(user, POST, parentId);
    }

    public Post createPost(Long boardId, PostDTO postDTO) {
        User user = userService.getUserSecured();
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
                checkApplyWebsiteAccessible(applyWebsite);
                post.setApplyWebsite(applyWebsite);
            }

            post.setApplyEmail(postDTO.getApplyEmail());

            DocumentDTO applyDocument = postDTO.getApplyDocument();
            if (applyDocument != null) {
                post.setApplyDocument(documentService.getOrCreateDocument(applyDocument));
            }

            checkPostApply(post);

            LocalDateTime liveTimestamp = postDTO.getLiveTimestamp();
            LocalDateTime deadTimestamp = postDTO.getDeadTimestamp();
            post.setLiveTimestamp(liveTimestamp);
            post.setDeadTimestamp(deadTimestamp);
            post = postRepository.save(post);

            resourceService.createResourceRelation(board, post);
            updatePostCategories(post, postDTO.getPostCategories());
            updateMemberCategories(post, toStrings(postDTO.getMemberCategories()));
            setIndexDataAndQuarter(post);

            userRoleService.getOrCreateUserRole(post, user, ADMINISTRATOR);
            resourceTaskService.completeTasks(department, POST_TASKS);
            return post;
        });

        addPostResponseReadiness(createdPost, user);
        if (createdPost.getState() == DRAFT && createdPost.getExistingRelation() == null) {
            throw new BoardException(MISSING_POST_EXISTING_RELATION, "Existing relation explanation required");
        }

        return createdPost;
    }

    public Post executeAction(Long id, Action action, PostPatchDTO postDTO) {
        User user = userService.getUserSecured();
        Post post = (Post) resourceService.getResource(user, POST, id);
        post.setComment(postDTO.getComment());
        return (Post) actionService.executeAction(user, post, action, () -> {
            if (action == EDIT) {
                updatePost(post, postDTO);
            } else {
                if (action == ACCEPT) {
                    Resource department = post.getParent().getParent();
                    userService.getByUserRoleWithoutUserRole(post, ADMINISTRATOR, department, AUTHOR)
                        .forEach(author -> userRoleService.createUserRole(department, author, AUTHOR));
                }

                if (hasUpdates(postDTO)) {
                    actionService.executeAction(user, post, EDIT, () -> {
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

    public void publishAndRetirePosts(LocalDateTime baseline) {
        List<Long> postToRetireIds = postRepository.findPostsToRetire(ACCEPTED_STATES, baseline);
        retirePosts(postToRetireIds, baseline);
        List<Long> postToPublishIds = postRepository.findPostsToPublish(PENDING_STATES, REJECTED, baseline);
        publishPosts(postToPublishIds, baseline);
    }

    public Map<String, Object> mapExistingRelationExplanation(String existingRelationExplanation) {
        if (existingRelationExplanation == null) {
            return null;
        }

        try {
            return objectMapper.readValue(existingRelationExplanation,
                new TypeReference<LinkedHashMap<String, Object>>() {
                });
        } catch (IOException e) {
            throw new BoardException(
                CORRUPTED_POST_EXISTING_RELATION_EXPLANATION, "Unable to deserialize existing relation explanation", e);
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

    public List<Post> getPosts(Long boardId) {
        return getPosts(boardId, true, null, null, null);
    }

    public PostStatistics getPostStatistics(Long departmentId) {
        return postDAO.getPostStatistics(departmentId);
    }

    @SuppressWarnings({"OptionalAssignedToNull", "ConstantConditions"})
    private void updatePost(Post post, PostPatchDTO postDTO) {
        post.setChangeList(new ChangeListRepresentation());
        postPatchService.patchProperty(post, "name", post::getName, post::setName, postDTO.getName());
        postPatchService.patchProperty(post, "summary",
            post::getSummary, post::setSummary, postDTO.getSummary());
        postPatchService.patchProperty(post, "description",
            post::getDescription, post::setDescription, postDTO.getDescription());
        postPatchService.patchOrganization(post, postDTO.getOrganization());
        postPatchService.patchLocation(post, postDTO.getLocation());

        Optional<String> applyWebsite = postDTO.getApplyWebsite();
        if (isPresent(applyWebsite)) {
            checkApplyWebsiteAccessible(applyWebsite.get());
            patchPostApply(post, applyWebsite, empty(), empty());
        }

        Optional<DocumentDTO> applyDocument = postDTO.getApplyDocument();
        if (isPresent(applyDocument)) {
            patchPostApply(post, empty(), applyDocument, empty());
        }

        Optional<String> applyEmail = postDTO.getApplyEmail();
        if (isPresent(applyEmail)) {
            patchPostApply(post, empty(), empty(), applyEmail);
        }

        patchPostCategories(post, postDTO.getPostCategories());
        patchMemberCategories(post, postDTO.getMemberCategories());

        postPatchService.patchProperty(post, "existingRelation",
            post::getExistingRelation, post::setExistingRelation, postDTO.getExistingRelation());
        patchExistingRelationExplanation(post, postDTO.getExistingRelationExplanation());

        Optional<LocalDateTime> liveTimestamp = postDTO.getLiveTimestamp();
        Optional<LocalDateTime> deadTimestamp = postDTO.getDeadTimestamp();
        postPatchService.patchProperty(post, "liveTimestamp", post::getLiveTimestamp, post::setLiveTimestamp,
            liveTimestamp != null ? liveTimestamp.map(t -> t.truncatedTo(ChronoUnit.SECONDS)) : null);
        postPatchService.patchProperty(post, "deadTimestamp", post::getDeadTimestamp, post::setDeadTimestamp,
            deadTimestamp != null ? deadTimestamp.map(t -> t.truncatedTo(ChronoUnit.SECONDS)) : null);

        setIndexDataAndQuarter(post);
    }

    public void archivePosts() {
        LocalDateTime baseline = LocalDateTime.now();
        List<Long> postIds = resourceService.getResourcesToArchive(baseline);
        if (!postIds.isEmpty()) {
            actionService.executeAnonymously(postIds, ARCHIVE, ARCHIVED, baseline);
        }
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private void patchPostApply(Post post, Optional<String> applyWebsite, Optional<DocumentDTO> applyDocument,
                                Optional<String> applyEmail) {
        postPatchService.patchProperty(post, "applyWebsite",
            post::getApplyWebsite, post::setApplyWebsite, applyWebsite);
        postPatchService.patchDocument(post, "applyDocument",
            post::getApplyDocument, post::setApplyDocument, applyDocument);
        postPatchService.patchProperty(post, "applyEmail",
            post::getApplyEmail, post::setApplyEmail, applyEmail);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private void patchPostCategories(Post post, Optional<List<String>> categories) {
        if (categories != null) {
            List<String> oldCategories = resourceService.getCategories(post, CategoryType.POST);
            if (categories.isPresent()) {
                List<String> newCategories = categories.get();
                if (!Objects.equals(oldCategories, newCategories)) {
                    updatePostCategories(post, newCategories);
                    post.getChangeList().put("postCategories",
                        oldCategories, resourceService.getCategories(post, CategoryType.POST));
                }
            } else if (oldCategories != null) {
                updatePostCategories(post, null);
                post.getChangeList().put("postCategories",
                    oldCategories, resourceService.getCategories(post, CategoryType.POST));
            }
        }
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private void patchMemberCategories(Post post, Optional<List<MemberCategory>> categories) {
        if (categories != null) {
            List<String> oldCategories = resourceService.getCategories(post, MEMBER);
            if (categories.isPresent()) {
                List<String> newCategories =
                    categories.get()
                        .stream()
                        .map(MemberCategory::name)
                        .collect(toList());

                if (!Objects.equals(oldCategories, newCategories)) {
                    updateMemberCategories(post, newCategories);
                    post.getChangeList().put("memberCategories",
                        oldCategories, resourceService.getCategories(post, MEMBER));
                }
            } else if (oldCategories != null) {
                updatePostCategories(post, null);
                post.getChangeList().put("memberCategories",
                    oldCategories, resourceService.getCategories(post, MEMBER));
            }
        }
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private void patchExistingRelationExplanation(Post post,
                                                  Optional<LinkedHashMap<String, Object>> existingRelationExplanation) {
        if (existingRelationExplanation != null) {
            String newValue = null;
            LinkedHashMap<String, Object> newValueMap = existingRelationExplanation.orElse(null);
            if (newValueMap != null) {
                newValue = mapExistingRelationExplanation(newValueMap);
            }

            String oldValue = post.getExistingRelationExplanation();
            if (!Objects.equals(oldValue, newValue)) {
                post.setExistingRelationExplanation(newValue);
                Map<String, Object> oldValueMap = oldValue == null ? null : mapExistingRelationExplanation(oldValue);
                post.getChangeList().put("existingRelationExplanation", oldValueMap, newValueMap);
            }
        }
    }

    private void updatePostCategories(Post post, List<String> categories) {
        Board board = (Board) post.getParent();
        List<String> postCategories = resourceService.getCategories(board, CategoryType.POST);
        checkCategories(categories, postCategories,
            CORRUPTED_POST_POST_CATEGORIES, MISSING_POST_POST_CATEGORIES, INVALID_POST_POST_CATEGORIES);
        resourceService.updateCategories(post, CategoryType.POST, categories);
    }

    private void updateMemberCategories(Post post, List<String> categories) {
        Department department = (Department) post.getParent().getParent();
        List<String> memberCategories = resourceService.getCategories(department, MEMBER);
        checkCategories(categories, memberCategories,
            CORRUPTED_POST_MEMBER_CATEGORIES, MISSING_POST_MEMBER_CATEGORIES, INVALID_POST_MEMBER_CATEGORIES);
        resourceService.updateCategories(post, MEMBER, categories);
    }

    private void retirePosts(List<Long> postIds, LocalDateTime baseline) {
        if (!postIds.isEmpty()) {
            actionService.executeAnonymously(postIds, RETIRE, EXPIRED, baseline);

            postIds.forEach(postId ->
                eventProducer.produce(
                    new ActivityEvent(this, postId,
                        singletonList(
                            new Activity()
                                .setScope(POST)
                                .setRole(ADMINISTRATOR)
                                .setActivity(RETIRE_POST_ACTIVITY))),
                    new NotificationEvent(this, postId,
                        singletonList(
                            new Notification()
                                .setScope(POST)
                                .setRole(ADMINISTRATOR)
                                .setNotification(RETIRE_POST_NOTIFICATION)))));
        }
    }

    private void publishPosts(List<Long> postIds, LocalDateTime baseline) {
        if (!postIds.isEmpty()) {
            actionService.executeAnonymously(postIds, PUBLISH, ACCEPTED, baseline);

            postIds.forEach(postId ->
                eventProducer.produce(
                    new ActivityEvent(this, postId,
                        ImmutableList.of(
                            new Activity()
                                .setScope(POST)
                                .setRole(ADMINISTRATOR)
                                .setActivity(PUBLISH_POST_ACTIVITY),
                            new Activity()
                                .setScope(DEPARTMENT)
                                .setRole(Role.MEMBER)
                                .setActivity(PUBLISH_POST_MEMBER_ACTIVITY))),
                    new NotificationEvent(this, postId,
                        ImmutableList.of(
                            new Notification()
                                .setScope(POST)
                                .setRole(ADMINISTRATOR)
                                .setNotification(PUBLISH_POST_NOTIFICATION),
                            new Notification()
                                .setScope(DEPARTMENT)
                                .setRole(Role.MEMBER)
                                .setNotification(PUBLISH_POST_MEMBER_NOTIFICATION)))));
        }
    }

    private String mapExistingRelationExplanation(Map<String, Object> existingRelationExplanation) {
        if (existingRelationExplanation == null) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(existingRelationExplanation);
        } catch (JsonProcessingException e) {
            throw new BoardException(
                CORRUPTED_POST_EXISTING_RELATION_EXPLANATION, "Unable to serialize existing relation explanation", e);
        }
    }

    private void addPostResponseReadiness(Post post, User user) {
        if (user != null) {
            boolean canPursue = actionService.canExecuteAction(post, PURSUE);
            DemographicDataStatus responseReadiness =
                departmentUserService.makeDemographicDataStatus(user, (Department) post.getParent().getParent());
            post.setDemographicDataStatus(responseReadiness);
            if (canPursue && responseReadiness.isReady() && post.getApplyEmail() == null) {
                resourceEventService.createPostReferral(post, user);
            }
        }
    }

    private void addPostResponse(Post post, User user) {
        if (user != null) {
            entityManager.flush();
            post.setExposeApplyData(actionService.canExecuteAction(post, EDIT));
            post.setReferral(resourceEventService.findByResourceAndEventAndUser(post, REFERRAL, user));
            post.setResponse(resourceEventService.findByResourceAndEventAndUser(post, RESPONSE, user));
        }
    }

    private void addPostResponsesAndReadinesses(User user, List<Post> posts) {
        if (user != null) {
            entityManager.flush();
            Map<Post, Post> postIndex =
                posts.stream()
                    .collect(toMap(post -> post, post -> post));

            Map<Resource, ResourceEvent> referrals =
                resourceEventService.findByResourceIdsAndEventAndUser(posts, REFERRAL, user)
                    .stream()
                    .collect(toMap(ResourceEvent::getResource, identity()));

            Map<Resource, ResourceEvent> responses =
                resourceEventService.findByResourceIdsAndEventAndUser(posts, RESPONSE, user)
                    .stream()
                    .collect(toMap(ResourceEvent::getResource, identity()));

            for (Map.Entry<Post, Post> postIndexEntry : postIndex.entrySet()) {
                Post post = postIndexEntry.getValue();
                post.setExposeApplyData(actionService.canExecuteAction(post, EDIT));
                post.setReferral(referrals.get(post));
                post.setResponse(responses.get(post));
            }
        }
    }

    private void checkPostApply(Post post) {
        long applyCount =
            Stream.of(post.getApplyWebsite(), post.getApplyDocument(), post.getApplyEmail())
                .filter(Objects::nonNull)
                .count();

        if (applyCount == 0) {
            throw new BoardException(MISSING_POST_APPLY, "No apply mechanism specified");
        } else if (applyCount > 1) {
            throw new BoardException(CORRUPTED_POST_APPLY, "Multiple apply mechanisms specified");
        }
    }

    private void checkApplyWebsiteAccessible(String applyWebsite) {
        try {
            URL url = new URL(applyWebsite);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setInstanceFollowRedirects(true);
            connection.connect();

            int responseCode = connection.getResponseCode();
            if (responseCode < 200 || responseCode >= 400) {
                throw new BoardException(INACCESSIBLE_POST_APPLY, "Cannot access apply website");
            }
        } catch (IOException e) {
            throw new BoardException(INACCESSIBLE_POST_APPLY, "Cannot access apply website");
        }
    }

    private void checkCategories(List<String> categories, List<String> referenceCategories,
                                 ExceptionCode exceptionCodeWhenCorrupted, ExceptionCode exceptionCodeWhenMissing,
                                 ExceptionCode exceptionCodeWhenInvalid) {
        if (referenceCategories.isEmpty()) {
            if (isNotEmpty(categories)) {
                throw new BoardException(exceptionCodeWhenCorrupted, "Categories must not be specified");
            }
        } else {
            if (isEmpty(categories)) {
                throw new BoardException(exceptionCodeWhenMissing, "Categories must be specified");
            }

            if (!referenceCategories.containsAll(categories)) {
                throw new BoardException(exceptionCodeWhenInvalid,
                    "Valid categories must be specified - check parent categories");
            }
        }
    }

}
