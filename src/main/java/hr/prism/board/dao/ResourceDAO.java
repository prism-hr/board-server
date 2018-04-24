package hr.prism.board.dao;

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.LinkedHashMultimap;
import hr.prism.board.domain.*;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.Scope;
import hr.prism.board.enums.State;
import hr.prism.board.exception.BoardDuplicateException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.repository.ResourceSearchRepository;
import hr.prism.board.representation.ActionRepresentation;
import hr.prism.board.value.ResourceFilter;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static com.google.common.collect.Lists.newArrayList;
import static hr.prism.board.enums.Role.PUBLIC;
import static hr.prism.board.enums.Scope.DEPARTMENT;
import static hr.prism.board.enums.State.ACTIVE_USER_ROLE_STATE_STRINGS;
import static hr.prism.board.enums.State.ARCHIVED;
import static hr.prism.board.utils.BoardUtils.makeSoundex;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Comparator.naturalOrder;
import static java.util.UUID.randomUUID;
import static org.slf4j.LoggerFactory.getLogger;

@Repository
@Transactional
public class ResourceDAO {

    private static final Logger LOGGER = getLogger(ResourceDAO.class);

    private static final String PUBLIC_RESOURCE =
        "SELECT resource.id, workflow.action, workflow.resource3_scope, workflow.resource3_state, " +
            "workflow.resource4_state, workflow.activity, workflow.notification " +
            "FROM resource " +
            "INNER join workflow " +
            "ON resource.scope = workflow.resource2_scope " +
            "AND resource.state = workflow.resource2_state " +
            "INNER JOIN resource_relation AS owner_relation " +
            "ON resource.id = owner_relation.resource2_id " +
            "AND (resource.scope = :departmentScope " +
            "OR owner_relation.resource1_id <> owner_relation.resource2_id) " +
            "INNER JOIN resource as owner " +
            "ON owner_relation.resource1_id = owner.id";

    private static final String SECURE_RESOURCE =
        "SELECT resource.id, workflow.action, workflow.resource3_scope, workflow.resource3_state, " +
            "workflow.resource4_state, workflow.activity, workflow.notification " +
            "FROM resource " +
            "INNER JOIN workflow " +
            "ON resource.scope = workflow.resource2_scope " +
            "AND resource.state = workflow.resource2_state " +
            "INNER JOIN resource_relation AS owner_relation " +
            "ON resource.id = owner_relation.resource2_id " +
            "AND (resource.scope = :departmentScope " +
            "OR owner_relation.resource1_id <> owner_relation.resource2_id) " +
            "INNER JOIN resource as owner " +
            "ON owner_relation.resource1_id = owner.id " +
            "INNER JOIN resource_relation " +
            "ON resource.id = resource_relation.resource2_id " +
            "INNER JOIN resource as parent " +
            "ON resource_relation.resource1_id = parent.id " +
            "AND workflow.resource1_scope = parent.scope " +
            "INNER JOIN user_role " +
            "ON parent.id = user_role.resource_id " +
            "AND workflow.role = user_role.role " +
            "AND user_role.state IN (:userRoleStates) " +
            "AND (user_role.expiry_date IS NULL OR user_role.expiry_date >= :baseline)";

    @SuppressWarnings("SqlResolve")
    private static final String ARCHIVE_RESOURCE =
        "SELECT DISTINCT resource.quarter " +
            "FROM resource " +
            "INNER JOIN workflow " +
            "ON resource.scope = workflow.resource2_scope " +
            "AND resource.state = workflow.resource2_state " +
            "INNER JOIN resource_relation AS owner_relation " +
            "ON resource.id = owner_relation.resource2_id " +
            "AND (resource.scope = :departmentScope " +
            "OR owner_relation.resource1_id <> owner_relation.resource2_id) " +
            "INNER JOIN resource as owner " +
            "ON owner_relation.resource1_id = owner.id " +
            "INNER JOIN resource_relation " +
            "ON resource.id = resource_relation.resource2_id " +
            "INNER JOIN resource as parent " +
            "ON resource_relation.resource1_id = parent.id " +
            "AND workflow.resource1_scope = parent.scope " +
            "INNER JOIN user_role " +
            "ON parent.id = user_role.resource_id " +
            "AND workflow.role = user_role.role " +
            "AND user_role.state IN (:userRoleStates) " +
            "AND (user_role.expiry_date IS NULL " +
            "OR user_role.expiry_date >= :baseline) " +
            "WHERE resource.scope = :scope " +
            "AND user_role.user_id = :userId " +
            "AND resource.state = :archiveState";

    private final ResourceSearchRepository resourceSearchRepository;

    private final EntityManager entityManager;

    @Inject
    public ResourceDAO(ResourceSearchRepository resourceSearchRepository, EntityManager entityManager) {
        this.resourceSearchRepository = resourceSearchRepository;
        this.entityManager = entityManager;
    }

    public List<Resource> getResources(User user, ResourceFilter filter) {
        List<String> publicFilterStatements = new ArrayList<>();
        publicFilterStatements.add("workflow.role = :role ");

        Map<String, Object> publicFilterParameters = new HashMap<>();
        publicFilterParameters.put("role", PUBLIC.name());
        publicFilterParameters.put("departmentScope", DEPARTMENT.name());

        List<String> secureFilterStatements = new ArrayList<>();
        Map<String, Object> secureFilterParameters = new HashMap<>();
        prepareDefaultFilters(user, secureFilterStatements, secureFilterParameters);
        prepareCustomFilters(
            filter, publicFilterStatements, publicFilterParameters, secureFilterStatements, secureFilterParameters);

        entityManager.flush();
        List<Object[]> publicResources = getResources(PUBLIC_RESOURCE, publicFilterStatements, publicFilterParameters);
        List<Object[]> secureResources = getResources(SECURE_RESOURCE, secureFilterStatements, secureFilterParameters);

        Map<ResourceAction, ActionRepresentation> rowIndex =
            mergePublicAndSecureResources(filter, publicResources, secureResources);
        if (rowIndex.isEmpty()) {
            return Collections.emptyList();
        }

        // Squash the mappings
        LinkedHashMultimap<Long, ActionRepresentation> resourceActionIndex = LinkedHashMultimap.create();
        rowIndex.keySet().forEach(key -> resourceActionIndex.put(key.id, rowIndex.get(key)));
        return getResources(filter, resourceActionIndex);
    }

    @SuppressWarnings({"SqlResolve", "unchecked"})
    public List<String> getResourceArchiveQuarters(User user, Scope scope, Long parentId) {
        List<String> filterStatements = new ArrayList<>();
        Map<String, Object> filterParameters = new HashMap<>();
        prepareDefaultFilters(user, filterStatements, filterParameters);

        filterParameters.put("scope", scope.name());
        filterParameters.put("archiveState", ARCHIVED.name());

        String statement = ARCHIVE_RESOURCE;
        if (parentId != null) {
            statement =
                statement + " " +
                    "AND resource.parent_id = :parentId";
            filterParameters.put("parentId", parentId);
        }

        statement =
            statement + " " +
                "ORDER BY resource.quarter desc";

        Query query = entityManager.createNativeQuery(statement);
        filterParameters.keySet().forEach(key -> query.setParameter(key, filterParameters.get(key)));
        return (List<String>) query.getResultList();
    }

    @SuppressWarnings("JpaQlInspection")
    public List<ResourceOperation> getResourceOperations(Resource resource) {
        return entityManager.createQuery(
            "select resourceOperation " +
                "from ResourceOperation resourceOperation " +
                "where resourceOperation.resource = :resource " +
                "order by resourceOperation.id desc", ResourceOperation.class)
            .setParameter("resource", resource)
            .setHint("javax.persistence.fetchgraph",
                entityManager.getEntityGraph("resource.operation"))
            .getResultList();
    }

    @SuppressWarnings({"unchecked", "JpaQlInspection"})
    public void checkUniqueName(Scope scope, Long id, Resource parent, String name, ExceptionCode exceptionCode) {
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

        Query query = entityManager.createQuery(statement, Long.class)
            .setParameter("scope", scope)
            .setParameter("name", name);
        constraints.keySet().forEach(key -> query.setParameter(key, constraints.get(key)));

        List<Long> resourceIds = (List<Long>) query.getResultList();
        if (!resourceIds.isEmpty()) {
            throw new BoardDuplicateException(exceptionCode,
                scope + " with name " + name + " exists already", resourceIds.get(0));
        }
    }

    @SuppressWarnings("JpaQlInspection")
    public void checkUniqueHandle(Resource resource, String handle, ExceptionCode exceptionCode) {
        Query query = entityManager.createQuery(
            "select resource.id " +
                "from Resource resource " +
                "where resource.handle = :handle " +
                "and resource.id <> :id")
            .setParameter("handle", handle)
            .setParameter("id", resource.getId());

        if (!query.getResultList().isEmpty()) {
            throw new BoardDuplicateException(exceptionCode, "Specified handle would not be unique");
        }
    }

    private List<Object[]> getResources(String statement, List<String> filterStatements,
                                        Map<String, Object> filterParameters) {
        Query query = entityManager.createNativeQuery(
            Joiner.on(" WHERE ").skipNulls().join(statement, Joiner.on(" AND ").join(filterStatements)));
        filterParameters.keySet().forEach(key -> query.setParameter(key, filterParameters.get(key)));

        //noinspection unchecked
        return query.getResultList();
    }

    private void prepareDefaultFilters(User user, List<String> secureFilterStatements,
                                       Map<String, Object> secureFilterParameters) {
        secureFilterStatements.add("user_role.user_id = :userId ");

        secureFilterParameters.put("userId", user == null ? "0" : user.getId().toString());
        secureFilterParameters.put("departmentScope", DEPARTMENT.name());
        secureFilterParameters.put("userRoleStates", ACTIVE_USER_ROLE_STATE_STRINGS);
        secureFilterParameters.put("baseline", LocalDate.now());
    }

    private void prepareCustomFilters(ResourceFilter filter, List<String> publicFilterStatements,
                                      Map<String, Object> publicFilterParameters, List<String> secureFilterStatements,
                                      Map<String, Object> secureFilterParameters) {
        for (Field field : ResourceFilter.class.getDeclaredFields()) {
            try {
                field.setAccessible(true);
                Object value = field.get(filter);
                if (value != null) {
                    ResourceFilter.ResourceFilterProperty resourceFilter =
                        field.getAnnotation(ResourceFilter.ResourceFilterProperty.class);
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
    }

    private Map<ResourceAction, ActionRepresentation> mergePublicAndSecureResources(ResourceFilter filter,
                                                                                    List<Object[]> publicResults,
                                                                                    List<Object[]> secureResults) {
        List<Object[]> rows = new ArrayList<>(secureResults);
        if (BooleanUtils.isTrue(filter.getIncludePublicResources())) {
            // Return public and secure results
            rows.addAll(publicResults);
        } else {
            // Return secure results with public actions
            HashMultimap<Object, Object[]> publicResultsById = HashMultimap.create();
            publicResults.forEach(result -> publicResultsById.put(result[0], result));
            for (Object[] secureResult : secureResults) {
                rows.addAll(publicResultsById.get(secureResult[0]));
            }
        }

        // Remove duplicate mappings
        Map<ResourceAction, ActionRepresentation> rowIndex = new HashMap<>();
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

            State suppressedInOwnerState = null;
            Object column5 = row[4];
            if (column5 != null) {
                suppressedInOwnerState = State.valueOf(column5.toString());
            }

            String rowActivity = null;
            Object column6 = row[5];
            if (column6 != null) {
                rowActivity = column6.toString();
            }

            String rowNotification = null;
            Object column7 = row[6];
            if (column7 != null) {
                rowNotification = column7.toString();
            }

            // Find the mapping that provides the most direct state transition, varies by role
            ResourceAction rowKey = new ResourceAction(rowId, rowAction, rowScope);
            ActionRepresentation rowValue = rowIndex.get(rowKey);
            if (rowValue == null || ObjectUtils.compare(rowState, rowValue.getState()) > 0) {
                rowIndex.put(rowKey,
                    new ActionRepresentation()
                        .setAction(rowAction)
                        .setScope(rowScope)
                        .setState(rowState)
                        .setSuppressedInOwnerState(suppressedInOwnerState)
                        .setActivity(rowActivity)
                        .setNotification(rowNotification));
            }
        }

        return rowIndex;
    }

    private List<Resource> getResources(ResourceFilter filter,
                                        LinkedHashMultimap<Long, ActionRepresentation> resourceActionIndex) {
        Scope scope = filter.getScope();
        Class<? extends Resource> resourceClass = scope.resourceClass;
        EntityGraph entityGraph = entityManager.getEntityGraph(scope.name().toLowerCase() + ".extended");

        String search = randomUUID().toString();
        String searchTerm = filter.getSearchTerm();
        Collection<Long> resourceIds = resourceActionIndex.keySet();

        boolean searchTermApplied = searchTerm != null;
        if (searchTermApplied) {
            // Apply the search query
            resourceSearchRepository.insertBySearch(search, LocalDateTime.now(), makeSoundex(searchTerm), resourceIds);
            entityManager.flush();
        }

        // Get the resource data
        String statement =
            "select distinct resource " +
                "from " + resourceClass.getSimpleName() + " resource " +
                "left join resource.searches search on search.search = :search " +
                "where resource.id in (:resourceIds) ";
        if (searchTermApplied) {
            statement += "and search.id is not null ";
        }

        statement += Joiner.on(", ").skipNulls().join("order by search.id", filter.getOrderStatement());

        @SuppressWarnings("unchecked")
        List<Resource> resources = entityManager.createQuery(statement)
            .setParameter("search", search)
            .setParameter("resourceIds", resourceIds)
            .setHint("javax.persistence.fetchgraph", entityGraph)
            .getResultList();

        if (searchTermApplied) {
            resourceSearchRepository.deleteBySearch(search);
        }

        // Merge the output
        mergeResourcesWithActions(resources, resourceActionIndex);
        return resources;
    }

    private void mergeResourcesWithActions(List<Resource> resources,
                                           LinkedHashMultimap<Long, ActionRepresentation> resourceActionIndex) {
        for (Resource resource : resources) {
            // Find the states of the parents
            List<State> parentStates = Collections.emptyList();
            if (resource instanceof Post) {
                Resource board = resource.getParent();
                parentStates = asList(board.getState(), board.getParent().getState());
            } else if (resource instanceof Board) {
                parentStates = singletonList(resource.getParent().getState());
            }

            // Remove any actions that should be suppressed due to parent state
            List<ActionRepresentation> actionRepresentations = newArrayList(resourceActionIndex.get(resource.getId()));
            Iterator<ActionRepresentation> actionRepresentationIterator = actionRepresentations.iterator();
            while (actionRepresentationIterator.hasNext()) {
                ActionRepresentation actionRepresentation = actionRepresentationIterator.next();
                State suppressedInOwnerState = actionRepresentation.getSuppressedInOwnerState();
                if (parentStates.contains(suppressedInOwnerState)) {
                    actionRepresentationIterator.remove();
                }
            }

            actionRepresentations.sort(naturalOrder());
            resource.setActions(actionRepresentations);
        }
    }

    private static class ResourceAction {

        private Long id;

        private Action action;

        private Scope scope;

        ResourceAction(Long id, Action action, Scope scope) {
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

            ResourceAction other = (ResourceAction) object;
            return Objects.equals(id, other.id)
                && Objects.equals(action, other.action)
                && Objects.equals(scope, other.scope);
        }

    }

}
