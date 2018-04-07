package hr.prism.board.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import hr.prism.board.dao.ResourceDAO;
import hr.prism.board.domain.*;
import hr.prism.board.enums.*;
import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.repository.ResourceCategoryRepository;
import hr.prism.board.repository.ResourceOperationRepository;
import hr.prism.board.repository.ResourceRelationRepository;
import hr.prism.board.repository.ResourceRepository;
import hr.prism.board.representation.ChangeListRepresentation;
import hr.prism.board.utils.BoardUtils;
import hr.prism.board.value.ResourceFilter;
import hr.prism.board.value.ResourceSummary;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "SpringAutowiredFieldsWarningInspection", "unused"})
public class ResourceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceService.class);

    private static final int MAX_HANDLE_LENGTH = 25;

    @Value("${scheduler.on}")
    private Boolean schedulerOn;

    @Value("${resource.archive.duration.seconds}")
    private Long resourceArchiveDurationSeconds;

    @Inject
    private ResourceRepository resourceRepository;

    @Inject
    private ResourceDAO resourceDAO;

    @Inject
    private ResourceRelationRepository resourceRelationRepository;

    @Inject
    private ResourceCategoryRepository resourceCategoryRepository;

    @Inject
    private ResourceOperationRepository resourceOperationRepository;

    @Inject
    private ActionService actionService;

    @Inject
    private UserService userService;

    @Inject
    private EntityManager entityManager;

    @Inject
    private ObjectMapper objectMapper;

    public Resource findOne(Long id) {
        return resourceRepository.findOne(id);
    }

    public Resource getResource(User user, Scope scope, Long id) {
        List<Resource> resources = resourceDAO.getResources(user, new ResourceFilter().setScope(scope).setId(id).setIncludePublicResources(true));
        return resources.isEmpty() ? resourceRepository.findOne(id) : resources.get(0);
    }

    public Resource getResource(User user, Scope scope, String handle) {
        List<Resource> resources = resourceDAO.getResources(user, new ResourceFilter().setScope(scope).setHandle(handle).setIncludePublicResources(true));
        return resources.isEmpty() ? resourceRepository.findByHandle(handle) : resources.get(0);
    }

    public List<Resource> getResources(User user, ResourceFilter filter) {
        return resourceDAO.getResources(user, filter);
    }

    public List<ResourceOperation> getResourceOperations(Scope scope, Long id) {
        User user = userService.getCurrentUserSecured();
        Resource resource = getResource(user, scope, id);
        actionService.executeAction(user, resource, Action.EDIT, () -> resource);
        return resourceDAO.getResourceOperations(resource);
    }

    public List<String> getResourceArchiveQuarters(Scope scope, Long parentId) {
        User user = userService.getCurrentUserSecured();
        return resourceDAO.getResourceArchiveQuarters(user, scope, parentId);
    }

    public List<String> getCategories(Resource resource, CategoryType categoryType) {
        List<ResourceCategory> categories = resource.getCategories(categoryType);
        return categories == null ? null : categories.stream().map(ResourceCategory::getName).collect(Collectors.toList());
    }

    public void validateCategories(Resource reference, CategoryType type, List<String> categories, ExceptionCode missing, ExceptionCode invalid, ExceptionCode corrupted) {
        List<ResourceCategory> referenceCategories = reference.getCategories(type);
        if (!referenceCategories.isEmpty()) {
            if (CollectionUtils.isEmpty(categories)) {
                throw new BoardException(missing, "Categories must be specified");
            } else if (!referenceCategories.stream().map(ResourceCategory::getName).collect(Collectors.toList()).containsAll(categories)) {
                throw new BoardException(invalid, "Valid categories must be specified - check parent categories");
            }
        } else if (CollectionUtils.isNotEmpty(categories)) {
            throw new BoardException(corrupted, "Categories must not be specified");
        }
    }

    public ResourceOperation getLatestResourceOperation(Resource resource, Action action) {
        return resourceOperationRepository.findFirstByResourceAndActionOrderByIdDesc(resource, action);
    }

    public Resource findByResourceAndEnclosingScope(Resource resource, Scope scope) {
        return resourceRepository.findByResourceAndEnclosingScope(resource, scope);
    }

    public void setIndexDataAndQuarter(Resource resource) {
        setIndexDataAndQuarter(resource, resource.getName(), resource.getSummary());
    }

    public List<ResourceSummary> findSummaryByUserAndRole(User user, Role role) {
        return resourceRepository.findSummaryByUserAndRole(user, role);
    }

    public void archiveResources() {
        LocalDateTime baseline = LocalDateTime.now();
        List<Long> resourceIds = resourceRepository.findByStatesAndLessThanUpdatedTimestamp(
            State.RESOURCE_STATES_TO_ARCHIVE_FROM, baseline.minusSeconds(resourceArchiveDurationSeconds));
        if (!resourceIds.isEmpty()) {
            actionService.executeAnonymously(resourceIds, Action.ARCHIVE, State.ARCHIVED, baseline);
        }
    }

    public void validateUniqueName(Scope scope, Long id, Resource parent, String name, ExceptionCode exceptionCode) {
        resourceDAO.validateUniqueName(scope, id, parent, name, exceptionCode);
    }

    public void validateUniqueHandle(Resource resource, String handle, ExceptionCode exceptionCode) {
        resourceDAO.validateUniqueHandle(resource, handle, exceptionCode);
    }

    public List<Resource> getSuppressableResources(Scope scope, User user) {
        return resourceRepository.findByScopeAndUserAndRolesOrCategory(
            scope, user, Arrays.asList(Role.ADMINISTRATOR, Role.AUTHOR), CategoryType.MEMBER, State.ACTIVE_USER_ROLE_STATES);
    }

    @SuppressWarnings("ConstantConditions")
    public void setIndexDataAndQuarter(Resource resource, String... parts) {
        Resource parent = resource.getParent();
        if (resource.equals(parent)) {
            resource.setIndexData(BoardUtils.makeSoundex(parts));
        } else {
            resource.setIndexData(Joiner.on(" ").skipNulls().join(parent.getIndexData(), BoardUtils.makeSoundex(parts)));
        }

        LocalDateTime createdTimestamp = resource.getCreatedTimestamp();
        resource.setQuarter(Integer.toString(createdTimestamp.getYear()) + (int) Math.ceil((double) createdTimestamp.getMonthValue() / 3));
    }

    public void updateHandle(Resource resource, String newHandle) {
        String handle = resource.getHandle();
        resource.setHandle(newHandle);
        resourceRepository.updateHandle(handle, newHandle);
    }

    public void createResourceRelation(Resource resource1, Resource resource2) {
        entityManager.flush();
        entityManager.refresh(resource1);
        entityManager.refresh(resource2);

        int resource1Ordinal = resource1.getScope().ordinal();
        int resource2Ordinal = resource2.getScope().ordinal();

        if ((resource1Ordinal + resource2Ordinal) == 0 || resource1Ordinal == (resource2Ordinal - 1)) {
            resource2.setParent(resource1);
            resource1.getParents().stream().map(ResourceRelation::getResource1).forEach(parentResource -> commitResourceRelation(parentResource, resource2));
            commitResourceRelation(resource2, resource2);
            return;
        }

        throw new IllegalStateException("Incorrect use of method. First argument must be of direct parent scope of second argument. " +
            "Arguments passed were: " + Joiner.on(", ").join(resource1, resource2));
    }

    public void updateCategories(Resource resource, CategoryType type, List<String> categories) {
        // Delete the old records
        deleteResourceCategories(resource, type);
        Set<ResourceCategory> oldCategories = resource.getCategories();
        oldCategories.removeIf(next -> next.getType() == type);

        if (categories != null) {
            // Write the new records
            categories.forEach(category ->
                oldCategories.add(
                    createResourceCategory(
                        new ResourceCategory().setResource(resource).setName(category).setType(type))));
        }
    }

    public void updateState(Resource resource, State state) {
        State previousState = resource.getState();
        if (previousState == null) {
            previousState = state;
        }

        if (state == State.PREVIOUS) {
            throw new IllegalStateException("Previous state is anonymous - cannot be assigned to a resource");
        }

        resource.setState(state);
        resource.setPreviousState(previousState);
        if (resource.getStateChangeTimestamp() == null || !state.equals(previousState)) {
            resource.setStateChangeTimestamp(LocalDateTime.now());
        }

        entityManager.flush();
    }

    public void createResourceOperation(Resource resource, Action action, User user) {
        ResourceOperation resourceOperation = new ResourceOperation().setResource(resource).setAction(action).setUser(user);
        if (action == Action.EDIT) {
            ChangeListRepresentation changeList = resource.getChangeList();
            if (CollectionUtils.isNotEmpty(changeList)) {
                try {
                    resourceOperation.setChangeList(objectMapper.writeValueAsString(changeList));
                } catch (JsonProcessingException e) {
                    LOGGER.info("Could not serialize change list", e);
                }
            }
        } else {
            resourceOperation.setComment(resource.getComment());
        }

        resourceOperation = resourceOperationRepository.save(resourceOperation);
        resource.getOperations().add(resourceOperation);
        resourceRepository.update(resource);
    }

    public String createHandle(Resource parent, String name, SimilarHandleFinder similarHandleFinder) {
        String handle;
        if (parent == null) {
            handle = ResourceService.suggestHandle(name);
        } else {
            handle = parent.getHandle() + "/" + ResourceService.suggestHandle(name);
        }

        List<String> similarHandles = similarHandleFinder.find(handle);
        return ResourceService.confirmHandle(handle, similarHandles);
    }

    public static String suggestHandle(String name) {
        String suggestion = "";
        name = StringUtils.stripAccents(name.toLowerCase());
        String[] parts = name.split(" ");
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (StringUtils.isAlphanumeric(part)) {
                String newSuggestion;
                if (suggestion.length() > 0) {
                    newSuggestion = suggestion + "-" + part;
                } else {
                    newSuggestion = part;
                }

                if (newSuggestion.length() > MAX_HANDLE_LENGTH) {
                    if (i == 0) {
                        return newSuggestion.substring(0, MAX_HANDLE_LENGTH);
                    }

                    return suggestion;
                }

                suggestion = newSuggestion;
            }
        }

        return suggestion;
    }

    public static ResourceFilter makeResourceFilter(Scope scope, Long parentId, Boolean includePublicPosts, State state, String quarter, String searchTerm) {
        String stateString = null;
        String negatedStateString = State.ARCHIVED.name();
        if (state != null) {
            stateString = state.name();
            if (state == State.ARCHIVED) {
                negatedStateString = null;
                if (quarter == null) {
                    throw new BoardException(ExceptionCode.INVALID_RESOURCE_FILTER, "Cannot search archive without specifying quarter");
                }
            }
        }

        return new ResourceFilter()
            .setScope(scope)
            .setParentId(parentId)
            .setState(stateString)
            .setNegatedState(negatedStateString)
            .setQuarter(quarter)
            .setSearchTerm(searchTerm)
            .setIncludePublicResources(includePublicPosts);
    }

    private void commitResourceRelation(Resource resource1, Resource resource2) {
        ResourceRelation resourceRelation = new ResourceRelation().setResource1(resource1).setResource2(resource2);
        resourceRelationRepository.save(resourceRelation);

        resource1.getChildren().add(resourceRelation);
        resource2.getParents().add(resourceRelation);
    }

    private static String confirmHandle(String suggestedHandle, List<String> similarHandles) {
        if (similarHandles.contains(suggestedHandle)) {
            int ordinal = 2;
            int suggestedHandleLength = suggestedHandle.length();
            List<String> similarHandleSuffixes = similarHandles.stream()
                .map(similarHandle -> similarHandle.substring(suggestedHandleLength))
                .collect(Collectors.toList());
            for (String similarHandleSuffix : similarHandleSuffixes) {
                if (similarHandleSuffix.startsWith("-")) {
                    String[] parts = similarHandleSuffix.replaceFirst("-", "").split("-");

                    // We only care about creating a unique value in a formatted sequence
                    // We can ignore anything else that has been reformatted by an end user
                    if (parts.length == 1) {
                        String firstPart = parts[0];
                        if (StringUtils.isNumeric(firstPart)) {
                            ordinal = Integer.parseInt(firstPart) + 1;
                            break;
                        }
                    }
                }
            }

            return suggestedHandle + "-" + ordinal;
        }

        return suggestedHandle;
    }

    private ResourceCategory createResourceCategory(ResourceCategory resourceCategory) {
        return resourceCategoryRepository.save(resourceCategory);
    }

    private void deleteResourceCategories(Resource resource, CategoryType type) {
        resourceCategoryRepository.deleteByResourceAndType(resource, type);
    }

    public interface SimilarHandleFinder {
        List<String> find(String handle);
    }

}
