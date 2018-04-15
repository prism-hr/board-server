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
import hr.prism.board.value.ResourceFilter;
import hr.prism.board.value.ResourceSummary;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static hr.prism.board.enums.Action.ARCHIVE;
import static hr.prism.board.enums.Action.EDIT;
import static hr.prism.board.enums.CategoryType.MEMBER;
import static hr.prism.board.enums.Role.NON_MEMBER_ROLES;
import static hr.prism.board.enums.State.*;
import static hr.prism.board.utils.BoardUtils.makeSoundex;
import static hr.prism.board.utils.ResourceUtils.confirmHandle;
import static hr.prism.board.utils.ResourceUtils.suggestHandle;
import static java.lang.Math.ceil;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.slf4j.LoggerFactory.getLogger;

@Service
@Transactional
public class ResourceService {

    private static final Logger LOGGER = getLogger(ResourceService.class);

    private final Long resourceArchiveDurationSeconds;

    @Inject
    private final ResourceRepository resourceRepository;

    @Inject
    private final ResourceDAO resourceDAO;

    @Inject
    private final ResourceRelationRepository resourceRelationRepository;

    @Inject
    private final ResourceCategoryRepository resourceCategoryRepository;

    @Inject
    private final ResourceOperationRepository resourceOperationRepository;

    @Inject
    private final UserService userService;

    @Inject
    private final EntityManager entityManager;

    @Inject
    private final ObjectMapper objectMapper;

    @Inject
    public ResourceService(@Value("${resource.archive.duration.seconds}") Long resourceArchiveDurationSeconds,
                           ResourceRepository resourceRepository, ResourceDAO resourceDAO,
                           ResourceRelationRepository resourceRelationRepository,
                           ResourceCategoryRepository resourceCategoryRepository,
                           ResourceOperationRepository resourceOperationRepository, UserService userService,
                           EntityManager entityManager, ObjectMapper objectMapper) {
        this.resourceArchiveDurationSeconds = resourceArchiveDurationSeconds;
        this.resourceRepository = resourceRepository;
        this.resourceDAO = resourceDAO;
        this.resourceRelationRepository = resourceRelationRepository;
        this.resourceCategoryRepository = resourceCategoryRepository;
        this.resourceOperationRepository = resourceOperationRepository;
        this.userService = userService;
        this.entityManager = entityManager;
        this.objectMapper = objectMapper;
    }

    public Resource findOne(Long id) {
        return resourceRepository.findOne(id);
    }

    public Resource getResource(User user, Scope scope, Long id) {
        List<Resource> resources = resourceDAO.getResources(user,
            new ResourceFilter()
                .setScope(scope)
                .setId(id)
                .setIncludePublicResources(true));

        return resources.isEmpty() ? resourceRepository.findOne(id) : resources.get(0);
    }

    public Resource getResource(User user, Scope scope, String handle) {
        List<Resource> resources = resourceDAO.getResources(user,
            new ResourceFilter()
                .setScope(scope)
                .setHandle(handle)
                .setIncludePublicResources(true));

        return resources.isEmpty() ? resourceRepository.findByHandle(handle) : resources.get(0);
    }

    public List<Resource> getResources(User user, ResourceFilter filter) {
        return resourceDAO.getResources(user, filter);
    }

    public List<ResourceOperation> getResourceOperations(Scope scope, Long id) {
        User user = userService.getCurrentUserSecured();
        Resource resource = getResource(user, scope, id);
        actionService.executeAction(user, resource, EDIT, () -> resource);
        return resourceDAO.getResourceOperations(resource);
    }

    public List<String> getResourceArchiveQuarters(Scope scope, Long parentId) {
        User user = userService.getCurrentUserSecured();
        return resourceDAO.getResourceArchiveQuarters(user, scope, parentId);
    }

    public List<String> getCategories(Resource resource, CategoryType categoryType) {
        List<ResourceCategory> categories = resource.getCategories(categoryType);
        return categories == null ? null : categories.stream().map(ResourceCategory::getName).collect(toList());
    }

    public void validateCategories(Resource reference, CategoryType type, List<String> categories,
                                   ExceptionCode missing, ExceptionCode invalid, ExceptionCode corrupted) {
        List<ResourceCategory> referenceCategories = reference.getCategories(type);
        if (!referenceCategories.isEmpty()) {
            if (isEmpty(categories)) {
                throw new BoardException(missing, "Categories must be specified");
            } else if (
                !referenceCategories
                    .stream()
                    .map(ResourceCategory::getName)
                    .collect(toList())
                    .containsAll(categories)) {
                throw new BoardException(invalid, "Valid categories must be specified - check parent categories");
            }
        } else if (isNotEmpty(categories)) {
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
            RESOURCE_STATES_TO_ARCHIVE_FROM, baseline.minusSeconds(resourceArchiveDurationSeconds));
        if (!resourceIds.isEmpty()) {
            actionService.executeAnonymously(resourceIds, ARCHIVE, ARCHIVED, baseline);
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
            scope, user, NON_MEMBER_ROLES, MEMBER, ACTIVE_USER_ROLE_STATES);
    }

    public void setIndexDataAndQuarter(Resource resource, String... parts) {
        Resource parent = resource.getParent();
        if (resource.equals(parent)) {
            resource.setIndexData(makeSoundex(parts));
        } else {
            String soundex = makeSoundex(parts);
            requireNonNull(soundex, "soundex cannot be null");
            resource.setIndexData(Joiner.on(" ").skipNulls().join(parent.getIndexData(), soundex));
        }

        LocalDateTime createdTimestamp = resource.getCreatedTimestamp();
        resource.setQuarter(
            Integer.toString(createdTimestamp.getYear()) + (int) ceil((double) createdTimestamp.getMonthValue() / 3));
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
            resource1.getParents()
                .stream()
                .map(ResourceRelation::getResource1)
                .forEach(parentResource -> commitResourceRelation(parentResource, resource2));

            commitResourceRelation(resource2, resource2);
            return;
        }

        throw new IllegalStateException("First argument must be of direct parent scope of second argument. " +
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
                        new ResourceCategory()
                            .setResource(resource)
                            .setName(category)
                            .setType(type))));
        }
    }

    public void updateState(Resource resource, State state) {
        State previousState = resource.getState();
        if (previousState == null) {
            previousState = state;
        }

        if (state == PREVIOUS) {
            throw new IllegalStateException("Previous state is anonymous - cannot be assigned to a resource");
        }

        resource.setState(state);
        resource.setPreviousState(previousState);
        if (resource.getStateChangeTimestamp() == null || !state.equals(previousState)) {
            resource.setStateChangeTimestamp(LocalDateTime.now());
        }

        entityManager.flush();
    }

    public void updateStates(List<Long> resourceIds, Action action, State state, LocalDateTime baseline) {
        resourceRepository.updateStateByIds(resourceIds, state, baseline);
        resourceOperationRepository.insertByResourceIdsActionAndCreatedTimestamp(resourceIds, action.name(), baseline);
    }

    public void createResourceOperation(Resource resource, Action action, User user) {
        ResourceOperation resourceOperation =
            new ResourceOperation()
                .setResource(resource)
                .setAction(action)
                .setUser(user);

        if (action == EDIT) {
            ChangeListRepresentation changeList = resource.getChangeList();
            if (isNotEmpty(changeList)) {
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
            handle = suggestHandle(name);
        } else {
            handle = parent.getHandle() + "/" + suggestHandle(name);
        }

        List<String> similarHandles = similarHandleFinder.find(handle);
        return confirmHandle(handle, similarHandles);
    }

    private void commitResourceRelation(Resource resource1, Resource resource2) {
        ResourceRelation resourceRelation =
            new ResourceRelation()
                .setResource1(resource1)
                .setResource2(resource2);
        resourceRelationRepository.save(resourceRelation);

        resource1.getChildren().add(resourceRelation);
        resource2.getParents().add(resourceRelation);
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
