package hr.prism.board.service;

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import hr.prism.board.domain.Board;
import hr.prism.board.domain.Department;
import hr.prism.board.domain.Resource;
import hr.prism.board.domain.ResourceCategory;
import hr.prism.board.domain.ResourceOperation;
import hr.prism.board.domain.ResourceRelation;
import hr.prism.board.domain.User;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.CategoryType;
import hr.prism.board.enums.Role;
import hr.prism.board.enums.Scope;
import hr.prism.board.enums.State;
import hr.prism.board.exception.BoardDuplicateException;
import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.repository.ResourceCategoryRepository;
import hr.prism.board.repository.ResourceOperationRepository;
import hr.prism.board.repository.ResourceRelationRepository;
import hr.prism.board.repository.ResourceRepository;
import hr.prism.board.repository.ResourceSearchRepository;
import hr.prism.board.representation.ActionRepresentation;
import hr.prism.board.representation.ChangeListRepresentation;
import hr.prism.board.utils.BoardUtils;
import hr.prism.board.value.ResourceFilter;
import hr.prism.board.value.ResourceSummary;

@Service
@Transactional
@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "unchecked", "SpringAutowiredFieldsWarningInspection", "SqlResolve"})
public class ResourceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceService.class);

    private static final int MAX_HANDLE_LENGTH = 25;

    private static final String RESOURCE_COLUMN_LIST =
        "SELECT resource.id, workflow.action, workflow.resource3_scope, workflow.resource3_state, workflow.activity, workflow.notification";

    private static final String PUBLIC_RESOURCE_ACTION =
        RESOURCE_COLUMN_LIST + " " +
            "FROM resource " +
            "INNER join workflow " +
            "ON resource.scope = workflow.resource2_scope " +
            "AND resource.state = workflow.resource2_state " +
            "INNER JOIN resource_relation AS owner_relation " +
            "ON resource.id = owner_relation.resource2_id " +
            "AND (resource.scope = :departmentScope OR owner_relation.resource1_id <> owner_relation.resource2_id) " +
            "INNER JOIN resource as owner " +
            "ON owner_relation.resource1_id = owner.id " +
            "AND (workflow.resource4_state IS NULL OR workflow.resource4_state = owner.state)";

    private static final String SECURE_RESOURCE_ACTION =
        "FROM resource " +
            "INNER JOIN workflow " +
            "ON resource.scope = workflow.resource2_scope " +
            "AND resource.state = workflow.resource2_state " +
            "INNER JOIN resource_relation AS owner_relation " +
            "ON resource.id = owner_relation.resource2_id " +
            "AND (resource.scope = :departmentScope OR owner_relation.resource1_id <> owner_relation.resource2_id) " +
            "INNER JOIN resource as owner " +
            "ON owner_relation.resource1_id = owner.id " +
            "AND (workflow.resource4_state IS NULL OR workflow.resource4_state = owner.state) " +
            "INNER JOIN resource_relation " +
            "ON resource.id = resource_relation.resource2_id " +
            "INNER JOIN resource as parent " +
            "ON resource_relation.resource1_id = parent.id " +
            "AND workflow.resource1_scope = parent.scope " +
            "INNER JOIN user_role " +
            "ON parent.id = user_role.resource_id " +
            "AND workflow.role = user_role.role AND user_role.state IN (:userRoleStates) AND (user_role.expiry_date IS NULL OR user_role.expiry_date >= :baseline)";

    private static final String SECURE_QUARTER =
        "SELECT DISTINCT resource.quarter " +
            SECURE_RESOURCE_ACTION + " " +
            "WHERE resource.scope = :scope " +
            "AND user_role.user_id = :userId";

    @Value("${scheduler.on}")
    private Boolean schedulerOn;

    @Value("${resource.archive.duration.seconds}")
    private Long resourceArchiveDurationSeconds;

    @Inject
    private ResourceRepository resourceRepository;

    @Inject
    private ResourceRelationRepository resourceRelationRepository;

    @Inject
    private ResourceCategoryRepository resourceCategoryRepository;

    @Inject
    private ResourceOperationRepository resourceOperationRepository;

    @Inject
    private ResourceSearchRepository resourceSearchRepository;

    @Inject
    private ActionService actionService;

    @Inject
    private UserService userService;

    @Inject
    private ObjectMapper objectMapper;

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    @SuppressWarnings("SpringJavaAutowiringInspection")
    private PlatformTransactionManager platformTransactionManager;

    public Resource findOne(Long id) {
        return resourceRepository.findOne(id);
    }

    public Resource getResource(User user, Scope scope, Long id) {
        List<Resource> resources = getResources(user, new ResourceFilter().setScope(scope).setId(id).setIncludePublicResources(true));
        return resources.isEmpty() ? resourceRepository.findOne(id) : resources.get(0);
    }

    public Resource getResource(User user, Scope scope, String handle) {
        List<Resource> resources = getResources(user, new ResourceFilter().setScope(scope).setHandle(handle).setIncludePublicResources(true));
        return resources.isEmpty() ? resourceRepository.findByHandle(handle) : resources.get(0);
    }

    public List<String> getCategories(Resource resource, CategoryType categoryType) {
        List<ResourceCategory> categories = resource.getCategories(categoryType);
        return categories == null ? null : categories.stream().map(ResourceCategory::getName).collect(Collectors.toList());
    }

    @SuppressWarnings("JpaQlInspection")
    public List<ResourceOperation> getResourceOperations(Scope scope, Long id) {
        User user = userService.getCurrentUserSecured();
        Resource resource = getResource(user, scope, id);
        actionService.executeAction(user, resource, Action.EDIT, () -> resource);

        return new ArrayList<>(entityManager.createQuery(
            "select resourceOperation " +
                "from ResourceOperation resourceOperation " +
                "where resourceOperation.resource.id = :resourceId " +
                "order by resourceOperation.id desc", ResourceOperation.class)
            .setParameter("resourceId", id)
            .setHint("javax.persistence.loadgraph", entityManager.getEntityGraph("resource.operation"))
            .getResultList());
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

    public List<String> getResourceArchiveQuarters(Scope scope, Long parentId) {
        String statement =
            SECURE_QUARTER + " " +
                "AND resource.state = :archiveState";

        User user = userService.getCurrentUserSecured();
        Map<String, Object> filterParameters = getSecureFilterParameters(user);
        filterParameters.put("scope", scope.name());
        filterParameters.put("archiveState", State.ARCHIVED.name());

        if (parentId != null) {
            statement =
                statement + " " +
                    "AND resource.parent_id = :parentId";
            filterParameters.put("parentId", parentId);
        }

        statement =
            statement + " " +
                "order by resource.quarter desc";

        Query query = entityManager.createNativeQuery(statement);
        filterParameters.keySet().forEach(key -> query.setParameter(key, filterParameters.get(key)));
        return query.getResultList();
    }

    public List<ResourceSummary> findSummaryByUserAndRole(User user, Role role) {
        return resourceRepository.findSummaryByUserAndRole(user, role);
    }

    @Scheduled(initialDelay = 60000, fixedDelay = 60000)
    public void archiveResourcesScheduled() {
        if (BooleanUtils.isTrue(schedulerOn)) {
            archiveResources();
        }
    }

    public void archiveResources() {
        LocalDateTime baseline = LocalDateTime.now();
        List<Long> resourceIds = resourceRepository.findByStatesAndLessThanUpdatedTimestamp(
            State.RESOURCE_STATES_TO_ARCHIVE_FROM, baseline.minusSeconds(resourceArchiveDurationSeconds));
        if (!resourceIds.isEmpty()) {
            actionService.executeInBulk(resourceIds, Action.ARCHIVE, State.ARCHIVED, baseline);
        }
    }

    @SuppressWarnings("JpaQlInspection")
    void validateUniqueName(Scope scope, Long id, Resource parent, String name, ExceptionCode exceptionCode) {
        String statement =
            "select resource.id " +
                "from Resource resource " +
                "where resource.scope = :scope " +
                "and resource.name = :name";

        Map<String, Object> constraints = new HashMap<>();
        if (id != null) {
            statement += " and resource.id <> :id";
            constraints.put("id", id);
        }

        if (parent != null && !Objects.equals(scope, parent.getScope())) {
            statement += " and resource.parent = :parent";
            constraints.put("parent", parent);
        }

        Query query = entityManager.createQuery(statement)
            .setParameter("scope", scope)
            .setParameter("name", name);
        constraints.keySet().forEach(key -> query.setParameter(key, constraints.get(key)));

        List<Long> resourceIds = query.getResultList();
        if (!resourceIds.isEmpty()) {
            throw new BoardDuplicateException(exceptionCode, scope.name() + " with name " + name + " exists already", resourceIds.get(0));
        }
    }

    @SuppressWarnings("JpaQlInspection")
    void validateUniqueHandle(Resource resource, String handle, ExceptionCode exceptionCode) {
        Query query = entityManager.createQuery(
            "select resource.id " +
                "from Resource resource " +
                "where resource.handle = :handle " +
                "and resource.id <> :id")
            .setParameter("handle", handle)
            .setParameter("id", resource.getId());

        if (!new ArrayList<>(query.getResultList()).isEmpty()) {
            throw new BoardException(exceptionCode, "Specified handle would not be unique");
        }
    }

    List<Resource> getSuppressableResources(Scope scope, User user) {
        return resourceRepository.findByScopeAndUserAndRolesOrCategory(
            scope, user, Arrays.asList(Role.ADMINISTRATOR, Role.AUTHOR), CategoryType.MEMBER, State.ACTIVE_USER_ROLE_STATES);
    }

    void setIndexDataAndQuarter(Resource resource, String... parts) {
        Resource parent = resource.getParent();
        if (resource.equals(parent)) {
            resource.setIndexData(BoardUtils.makeSoundex(parts));
        } else {
            resource.setIndexData(Joiner.on(" ").join(parent.getIndexData(), BoardUtils.makeSoundex(parts)));
        }

        LocalDateTime createdTimestamp = resource.getCreatedTimestamp();
        resource.setQuarter(Integer.toString(createdTimestamp.getYear()) + (int) Math.ceil((double) createdTimestamp.getMonthValue() / 3));
    }

    void updateHandle(Resource resource, String newHandle) {
        String handle = resource.getHandle();
        resource.setHandle(newHandle);
        resourceRepository.updateHandle(handle, newHandle);
    }

    void createResourceRelation(Resource resource1, Resource resource2) {
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

    void updateCategories(Resource resource, CategoryType type, List<String> categories) {
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

    void updateState(Resource resource, State state) {
        State previousState = resource.getState();
        if (previousState == null) {
            previousState = state;
        }

        if (state == State.PREVIOUS) {
            throw new IllegalStateException("Previous state is anonymous - cannot be assigned to a resource");
        }

        resource.setState(state);
        resource.setPreviousState(previousState);

        entityManager.flush();
        Resource parent = resource.getParent();
        if (parent instanceof Department) {
            ResourceSummary summary = resourceRepository.findSummaryByParentAndState(parent, State.ACCEPTED);
            ((Department) parent).setBoardCount(summary.getCount());
        } else if (parent instanceof Board) {
            ResourceSummary summary = resourceRepository.findSummaryByParentAndState(parent, State.ACCEPTED);
            ((Board) parent).setPostCount(summary.getCount());
        }
    }

    List<Resource> getResources(User user, ResourceFilter filter) {
        List<String> publicFilterStatements = new ArrayList<>();
        publicFilterStatements.add("workflow.role = :role ");

        Map<String, Object> publicFilterParameters = new HashMap<>();
        publicFilterParameters.put("role", Role.PUBLIC.name());
        publicFilterParameters.put("departmentScope", Scope.DEPARTMENT.name());

        List<String> secureFilterStatements = new ArrayList<>();
        secureFilterStatements.add("user_role.user_id = :userId ");
        Map<String, Object> secureFilterParameters = getSecureFilterParameters(user);

        // Unwrap the filters
        for (Field field : ResourceFilter.class.getDeclaredFields()) {
            try {
                field.setAccessible(true);
                Object value = field.get(filter);
                if (value != null) {
                    ResourceFilter.ResourceFilterProperty resourceFilter = field.getAnnotation(ResourceFilter.ResourceFilterProperty.class);
                    if (resourceFilter != null) {
                        String statement = resourceFilter.statement();
                        String parameter = resourceFilter.parameter();

                        secureFilterStatements.add(statement);
                        secureFilterParameters.put(parameter, value.toString());

                        publicFilterStatements.add(statement);
                        publicFilterParameters.put(parameter, value.toString());
                    }
                }
            } catch (IllegalAccessException e) {
                LOGGER.error("Cannot access filter property: " + field.getName(), e);
            }
        }

        // Get the mappings
        entityManager.flush();
        TransactionTemplate transactionTemplate = new TransactionTemplate(platformTransactionManager);
        List<Object[]> rows = transactionTemplate.execute(status -> {
            List<Object[]> publicResults = getResources(PUBLIC_RESOURCE_ACTION, publicFilterStatements, publicFilterParameters);
            List<Object[]> secureResults = getResources(RESOURCE_COLUMN_LIST + " " + SECURE_RESOURCE_ACTION, secureFilterStatements, secureFilterParameters);
            if (BooleanUtils.isTrue(filter.getIncludePublicResources())) {
                // Return public and secure results
                secureResults.addAll(publicResults);
                return secureResults;
            }

            List<Object[]> securePublicResults = new ArrayList<>();
            HashMultimap<Object, Object[]> publicResultsIndex = HashMultimap.create();
            publicResults.forEach(result -> publicResultsIndex.put(result[0], result));
            for (Object[] secureResult : secureResults) {
                securePublicResults.addAll(publicResultsIndex.get(secureResult[0]));
            }

            // Return secure results with public actions
            secureResults.addAll(securePublicResults);
            return secureResults;
        });

        // Remove duplicate mappings
        Map<ResourceActionKey, ActionRepresentation> rowIndex = new HashMap<>();
        for (Object[] row : rows) {
            Long rowId = Long.parseLong(row[0].toString());
            Action rowAction = Action.valueOf(row[1].toString());

            Scope rowScope = null;
            Object column3 = row[2];
            if (column3 != null) {
                rowScope = Scope.valueOf(column3.toString());
            }

            State rowState = null;
            Object column4 = row[3];
            if (column4 != null) {
                rowState = State.valueOf(column4.toString());
            }

            String rowActivity = null;
            Object column5 = row[4];
            if (column5 != null) {
                rowActivity = column5.toString();
            }

            String rowNotification = null;
            Object column6 = row[5];
            if (column6 != null) {
                rowNotification = column6.toString();
            }

            // Find the mapping that provides the most direct state transition, varies by role
            ResourceActionKey rowKey = new ResourceActionKey(rowId, rowAction, rowScope);
            ActionRepresentation rowValue = rowIndex.get(rowKey);
            if (rowValue == null || ObjectUtils.compare(rowState, rowValue.getState()) > 0) {
                rowIndex.put(rowKey,
                    new ActionRepresentation().setAction(rowAction)
                        .setScope(rowScope)
                        .setState(rowState)
                        .setActivity(rowActivity)
                        .setNotification(rowNotification));
            }
        }

        if (rowIndex.isEmpty()) {
            return Collections.emptyList();
        }

        // Squash the mappings
        LinkedHashMultimap<Long, ActionRepresentation> resourceActionIndex = LinkedHashMultimap.create();
        rowIndex.keySet().forEach(key -> resourceActionIndex.put(key.id, rowIndex.get(key)));

        Scope scope = filter.getScope();
        Class<? extends Resource> resourceClass = scope.resourceClass;
        EntityGraph entityGraph = entityManager.getEntityGraph(scope.name().toLowerCase() + ".extended");

        String search = UUID.randomUUID().toString();
        String searchTerm = filter.getSearchTerm();
        Collection<Long> resourceIds = resourceActionIndex.keySet();

        boolean searchTermApplied = searchTerm != null;
        if (searchTermApplied) {
            // Apply the search query
            resourceSearchRepository.insertBySearch(search, LocalDateTime.now(), BoardUtils.makeSoundex(searchTerm), resourceIds);
            entityManager.flush();
        }

        // Get the resource data
        List<Resource> resources = transactionTemplate.execute(status -> {
            String statement =
                "select distinct resource " +
                    "from " + resourceClass.getSimpleName() + " resource " +
                    "left join resource.searches search on search.search = :search " +
                    "where resource.id in (:resourceIds) ";
            if (searchTermApplied) {
                statement += "and search.id is not null ";
            }

            statement += Joiner.on(", ").skipNulls().join("order by search.id", filter.getOrderStatement());

            return (List<Resource>) entityManager.createQuery(statement, resourceClass)
                .setParameter("search", search)
                .setParameter("resourceIds", resourceIds)
                .setHint("javax.persistence.loadgraph", entityGraph)
                .getResultList();
        });

        if (searchTermApplied) {
            resourceSearchRepository.deleteBySearch(search);
        }

        // Merge the output
        for (Resource resource : resources) {
            List<ActionRepresentation> actionRepresentations = Lists.newArrayList(resourceActionIndex.get(resource.getId()));
            actionRepresentations.sort(Comparator.naturalOrder());
            resource.setActions(actionRepresentations);
        }

        return resources;
    }

    ResourceOperation createResourceOperation(Resource resource, Action action, User user) {
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
        return resourceOperation;
    }

    String createHandle(Resource parent, String name, SimilarHandleFinder similarHandleFinder) {
        String handle;
        if (parent == null) {
            handle = ResourceService.suggestHandle(name);
        } else {
            handle = parent.getHandle() + "/" + ResourceService.suggestHandle(name);
        }

        List<String> similarHandles = similarHandleFinder.find(handle);
        return ResourceService.confirmHandle(handle, similarHandles);
    }

    static String suggestHandle(String name) {
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

    static ResourceFilter makeResourceFilter(Scope scope, Long parentId, Boolean includePublicPosts, State state, String quarter, String searchTerm) {
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

    private Map<String, Object> getSecureFilterParameters(User user) {
        Map<String, Object> secureFilterParameters = new HashMap<>();
        secureFilterParameters.put("userId", user == null ? "0" : user.getId().toString());
        secureFilterParameters.put("departmentScope", Scope.DEPARTMENT.name());
        secureFilterParameters.put("userRoleStates", State.ACTIVE_USER_ROLE_STATE_STRINGS);
        secureFilterParameters.put("baseline", LocalDate.now());
        return secureFilterParameters;
    }

    private List<Object[]> getResources(String statement, List<String> filterStatements, Map<String, Object> filterParameters) {
        Query query = entityManager.createNativeQuery(Joiner.on(" WHERE ").skipNulls().join(statement, Joiner.on(" AND ").join(filterStatements)));
        filterParameters.keySet().forEach(key -> query.setParameter(key, filterParameters.get(key)));
        return query.getResultList();
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

    private static class ResourceActionKey {

        private Long id;

        private Action action;

        private Scope scope;

        ResourceActionKey(Long id, Action action, Scope scope) {
            this.id = id;
            this.action = action;
            this.scope = scope;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, action, scope);
        }

        @Override
        public boolean equals(Object object) {
            if (object == null || getClass() != object.getClass()) {
                return false;
            }

            ResourceActionKey other = (ResourceActionKey) object;
            return Objects.equals(id, other.id) && Objects.equals(action, other.action) && Objects.equals(scope, other.scope);
        }

    }

}
