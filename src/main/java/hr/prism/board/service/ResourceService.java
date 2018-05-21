package hr.prism.board.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import hr.prism.board.dao.ResourceDAO;
import hr.prism.board.domain.*;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.CategoryType;
import hr.prism.board.enums.Scope;
import hr.prism.board.enums.State;
import hr.prism.board.exception.BoardNotFoundException;
import hr.prism.board.repository.ResourceCategoryRepository;
import hr.prism.board.repository.ResourceOperationRepository;
import hr.prism.board.repository.ResourceRelationRepository;
import hr.prism.board.repository.ResourceRepository;
import hr.prism.board.representation.ChangeListRepresentation;
import hr.prism.board.value.ResourceFilter;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

import static hr.prism.board.enums.Action.EDIT;
import static hr.prism.board.enums.CategoryType.MEMBER;
import static hr.prism.board.enums.Role.STAFF_ROLES;
import static hr.prism.board.enums.State.*;
import static hr.prism.board.exception.ExceptionCode.DUPLICATE_RESOURCE_HANDLE;
import static hr.prism.board.exception.ExceptionCode.MISSING_RESOURCE;
import static hr.prism.board.utils.BoardUtils.makeSoundex;
import static hr.prism.board.utils.ResourceUtils.*;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.slf4j.LoggerFactory.getLogger;

@Service
@Transactional
public class ResourceService {

    private static final Logger LOGGER = getLogger(ResourceService.class);

    private final Long resourceArchiveDurationSeconds;

    private final ResourceRepository resourceRepository;

    private final ResourceDAO resourceDAO;

    private final ResourceRelationRepository resourceRelationRepository;

    private final ResourceCategoryRepository resourceCategoryRepository;

    private final ResourceOperationRepository resourceOperationRepository;

    private final EntityManager entityManager;

    private final ObjectMapper objectMapper;

    @Inject
    public ResourceService(@Value("${resource.archive.duration.seconds}") Long resourceArchiveDurationSeconds,
                           ResourceRepository resourceRepository, ResourceDAO resourceDAO,
                           ResourceRelationRepository resourceRelationRepository,
                           ResourceCategoryRepository resourceCategoryRepository,
                           ResourceOperationRepository resourceOperationRepository, EntityManager entityManager,
                           ObjectMapper objectMapper) {
        this.resourceArchiveDurationSeconds = resourceArchiveDurationSeconds;
        this.resourceRepository = resourceRepository;
        this.resourceDAO = resourceDAO;
        this.resourceRelationRepository = resourceRelationRepository;
        this.resourceCategoryRepository = resourceCategoryRepository;
        this.resourceOperationRepository = resourceOperationRepository;
        this.entityManager = entityManager;
        this.objectMapper = objectMapper;
    }

    public Resource getById(Long id) {
        return resourceRepository.findOne(id);
    }

    public Resource getByHandle(String handle) {
        return resourceRepository.findByHandle(handle);
    }

    public Resource getResource(User user, Scope scope, Long id) {
        List<Resource> resources = resourceDAO.getResources(user,
            new ResourceFilter().setScope(scope).setId(id));

        return ofNullable(resources.isEmpty() ? resourceDAO.getById(scope, id) : resources.get(0))
            .orElseThrow(() -> new BoardNotFoundException(MISSING_RESOURCE, scope, id));
    }

    public Resource getResource(User user, Scope scope, String handle) {
        List<Resource> resources = resourceDAO.getResources(user,
            new ResourceFilter().setScope(scope).setHandle(handle));

        return ofNullable(resources.isEmpty() ? resourceDAO.getByHandle(scope, handle) : resources.get(0))
            .orElseThrow(() -> new BoardNotFoundException(MISSING_RESOURCE, scope, handle));
    }

    public List<Resource> getResources(User user, ResourceFilter filter) {
        return resourceDAO.getResources(user, filter);
    }

    public List<ResourceOperation> getResourceOperations(Resource resource) {
        return resourceDAO.getResourceOperations(resource);
    }

    public List<String> getResourceArchiveQuarters(User user, Scope scope, Long parentId) {
        return resourceDAO.getResourceArchiveQuarters(user, scope, parentId);
    }

    public ResourceOperation getLatestResourceOperation(Resource resource, Action action) {
        return resourceOperationRepository.findFirstByResourceAndActionOrderByIdDesc(resource, action);
    }

    public Resource getByResourceAndEnclosingScope(Resource resource, Scope scope) {
        return resourceRepository.findByResourceAndEnclosingScope(resource, scope);
    }

    public List<Long> getResourcesToArchive(LocalDateTime baseline) {
        return resourceRepository.findByStatesAndLessThanUpdatedTimestamp(
            RESOURCE_STATES_TO_ARCHIVE_FROM, baseline.minusSeconds(resourceArchiveDurationSeconds));
    }

    public void checkUniqueName(Resource resource, String name) {
        resourceDAO.checkUniqueName(resource.getScope(), resource.getId(), resource.getParent(), name);
    }

    public void checkUniqueHandle(Resource resource, String handle) {
        resourceDAO.checkUniqueHandle(resource, handle, DUPLICATE_RESOURCE_HANDLE);
    }

    public List<Resource> getSuppressableResources(Scope scope, User user) {
        return resourceRepository.findByScopeAndUserAndRolesOrCategory(
            scope, user, STAFF_ROLES, MEMBER, ACCEPTED_STATES);
    }

    public String createHandle(Resource resource) {
        String suggestedHandle = resource.getParent().getHandle() + "/" + suggestHandle(resource.getName());
        List<String> similarHandles =
            resourceRepository.findHandleLikeSuggestedHandle(resource.getScope(), suggestedHandle);
        return confirmHandle(suggestedHandle, similarHandles);
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
            resource1.getParents()
                .stream()
                .map(ResourceRelation::getResource1)
                .forEach(parentResource -> saveResourceRelation(parentResource, resource2));

            saveResourceRelation(resource2, resource2);
            return;
        }

        throw new IllegalStateException("First argument must be of direct parent scope of second argument. " +
            "Arguments passed were: " + Joiner.on(", ").join(resource1, resource2));
    }

    public void updateCategories(Resource resource, CategoryType type, List<String> categories) {
        deleteResourceCategories(resource, type);
        if (isNotEmpty(categories)) {
            categories.forEach(name ->
                createResourceCategory(
                    new ResourceCategory()
                        .setResource(resource)
                        .setType(type)
                        .setName(name)));
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

        resourceRepository.save(resource);
        entityManager.flush();
    }

    public void updateStates(List<Long> resourceIds, Action action, State state, LocalDateTime baseline) {
        resourceRepository.updateStateByIds(resourceIds, state, baseline);
        resourceOperationRepository.insertByResourceIdsActionAndCreatedTimestamp(resourceIds, action.name(), baseline);
    }

    @SuppressWarnings("UnusedReturnValue")
    public ResourceOperation createResourceOperation(Resource resource, Action action, User user) {
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

        return resourceOperationRepository.save(resourceOperation);
    }

    public void setIndexDataAndQuarter(Resource resource) {
        Resource parent = resource.getParent();
        if (resource.equals(parent)) {
            resource.setIndexData(makeSoundex(resource.getIndexDataParts()));
        } else {
            String soundex = makeSoundex(resource.getIndexDataParts());
            requireNonNull(soundex, "soundex cannot be null");
            resource.setIndexData(Joiner.on(" ").skipNulls().join(parent.getIndexData(), soundex));
        }

        LocalDateTime createdTimestamp = resource.getCreatedTimestamp();
        resource.setQuarter(getQuarter(createdTimestamp));
    }

    private void saveResourceRelation(Resource resource1, Resource resource2) {
        resourceRelationRepository.save(
            new ResourceRelation()
                .setResource1(resource1)
                .setResource2(resource2));
    }

    private ResourceCategory createResourceCategory(ResourceCategory resourceCategory) {
        return resourceCategoryRepository.save(resourceCategory);
    }

    private void deleteResourceCategories(Resource resource, CategoryType type) {
        resourceCategoryRepository.deleteByResourceAndType(resource, type);
    }

}
