package hr.prism.board.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import hr.prism.board.domain.*;
import hr.prism.board.dto.ResourceFilterDTO;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.CategoryType;
import hr.prism.board.enums.State;
import hr.prism.board.repository.ResourceCategoryRepository;
import hr.prism.board.repository.ResourceOperationRepository;
import hr.prism.board.repository.ResourceRelationRepository;
import hr.prism.board.repository.ResourceRepository;
import hr.prism.board.representation.ActionRepresentation;
import hr.prism.board.representation.ResourceChangeListRepresentation;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.inject.Inject;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ResourceService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceService.class);
    
    private static final String PUBLIC_RESOURCE_ACTION =
        "select resource.id, permission.action, " +
            "permission.resource3_scope, permission.resource3_state " +
            "from resource " +
            "inner join permission " +
            "on resource.scope = permission.resource2_scope " +
            "and resource.state = permission.resource2_state";
    
    private static final String SECURE_RESOURCE_ACTION =
        "select resource.id, permission.action, " +
            "permission.resource3_scope, permission.resource3_state " +
            "from resource " +
            "inner join permission " +
            "on resource.scope = permission.resource2_scope " +
            "and resource.state = permission.resource2_state " +
            "inner join resource_relation " +
            "on resource.id = resource_relation.resource2_id " +
            "inner join resource as parent " +
            "on resource_relation.resource1_id = parent.id " +
            "and permission.resource1_scope = parent.scope " +
            "inner join user_role " +
            "on parent.id = user_role.resource_id " +
            "and permission.role = user_role.role";
    
    @Inject
    private ResourceRepository resourceRepository;
    
    @Inject
    private ResourceRelationRepository resourceRelationRepository;
    
    @Inject
    private ResourceCategoryRepository resourceCategoryRepository;
    
    @Inject
    private ResourceOperationRepository resourceOperationRepository;
    
    @Inject
    private ObjectMapper objectMapper;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Inject
    @SuppressWarnings("SpringJavaAutowiringInspection")
    private PlatformTransactionManager platformTransactionManager;
    
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
            "Arguments passed where: " + Joiner.on(", ").join(resource1, resource2));
    }
    
    public void updateCategories(Resource resource, List<String> categories, CategoryType type) {
        HashSet<String> postedCategories = new HashSet<>(categories);
        Set<ResourceCategory> existingCategories = resource.getCategories();
        
        // modify existing categories
        for (ResourceCategory existingResourceCategory : existingCategories) {
            if (postedCategories.remove(existingResourceCategory.getName())) {
                existingResourceCategory.setActive(true);
            } else {
                // category was not in the posted list, make inactive
                existingResourceCategory.setActive(false);
            }
    
            resourceCategoryRepository.update(existingResourceCategory, LocalDateTime.now());
        }
        
        // add new categories
        for (String postedCategory : postedCategories) {
            ResourceCategory newResourceCategory = new ResourceCategory().setResource(resource).setName(postedCategory).setActive(true).setType(type);
            newResourceCategory.setCreatedTimestamp(LocalDateTime.now());
            resourceCategoryRepository.save(newResourceCategory);
            existingCategories.add(newResourceCategory);
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
    }
    
    public Resource getResource(User user, Scope scope, Long id) {
        List<Resource> resources = getResources(user, new ResourceFilterDTO().setScope(scope).setId(id));
        return resources.isEmpty() ? null : resources.get(0);
    }
    
    public Resource getResource(User user, Scope scope, String handle) {
        List<Resource> resources = getResources(user, new ResourceFilterDTO().setScope(scope).setHandle(handle));
        return resources.isEmpty() ? null : resources.get(0);
    }
    
    public List<Resource> getResources(User user, ResourceFilterDTO filter) {
        List<String> publicFilterStatements = new ArrayList<>();
        publicFilterStatements.add("permission.role = :role");
        Map<String, String> publicFilterParameters = new HashMap<>();
        publicFilterParameters.put("role", Role.PUBLIC.name());
        
        List<String> secureFilterStatements = new ArrayList<>();
        secureFilterStatements.add("user_role.user_id = :userId");
        Map<String, String> secureFilterParameters = new HashMap<>();
        secureFilterParameters.put("userId", user == null ? "0" : user.getId().toString());
        
        // Unwrap the filters
        for (Field field : ResourceFilterDTO.class.getDeclaredFields()) {
            try {
                field.setAccessible(true);
                Object value = field.get(filter);
                if (value != null) {
                    ResourceFilterDTO.ResourceFilter resourceFilter = field.getAnnotation(ResourceFilterDTO.ResourceFilter.class);
                    if (resourceFilter != null) {
                        String statement = resourceFilter.statement();
                        String parameter = resourceFilter.parameter();
    
                        secureFilterStatements.add(statement);
                        secureFilterParameters.put(parameter, value.toString());
                        
                        if (!resourceFilter.secured()) {
                            publicFilterStatements.add(statement);
                            publicFilterParameters.put(parameter, value.toString());
                        }
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
            List<Object[]> results = getResources(PUBLIC_RESOURCE_ACTION, publicFilterStatements, publicFilterParameters);
            results.addAll(getResources(SECURE_RESOURCE_ACTION, secureFilterStatements, secureFilterParameters));
            return results;
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
    
            // Find the mapping that provides the most direct state transition, varies by role
            ResourceActionKey rowKey = new ResourceActionKey(rowId, rowAction, rowScope);
            ActionRepresentation rowValue = rowIndex.get(rowKey);
            if (rowValue == null || ObjectUtils.compare(rowState, rowValue.getState()) > 0) {
                rowIndex.put(rowKey, new ActionRepresentation().setAction(rowAction).setScope(rowScope).setState(rowState));
            }
        }
        
        if (rowIndex.isEmpty()) {
            return Collections.emptyList();
        }
        
        // Squash the mappings
        LinkedHashMultimap<Long, ActionRepresentation> resourceActionIndex = LinkedHashMultimap.create();
        rowIndex.keySet().forEach(key -> resourceActionIndex.put((Long) key.id, rowIndex.get(key)));
        
        Scope scope = filter.getScope();
        Class<? extends Resource> resourceClass = scope.resourceClass;
        EntityGraph entityGraph = entityManager.getEntityGraph(scope.name().toLowerCase() + ".extended");
        
        // Get the resource data
        List<Resource> resources = transactionTemplate.execute(status -> {
            String statement = Joiner.on(" ").skipNulls().join(
                "select distinct resource " +
                    "from " + resourceClass.getSimpleName() + " resource " +
                    "where resource.id in (:ids)",
                filter.getOrderStatement());
    
            return new ArrayList<Resource>(entityManager.createQuery(statement, resourceClass)
                .setParameter("ids", resourceActionIndex.keySet())
                .setHint("javax.persistence.loadgraph", entityGraph)
                .getResultList());
        });
        
        // Merge the output
        for (Resource resource : resources) {
            List<ActionRepresentation> actionRepresentations = Lists.newArrayList(resourceActionIndex.get(resource.getId()));
            actionRepresentations.sort(Comparator.naturalOrder());
            resource.setActions(actionRepresentations);
        }
        
        return resources;
    }
    
    public static String suggestHandle(String name) {
        String suggestion = "";
        name = name.toLowerCase();
        String[] parts = name.split(" ");
        for (int i = 0; i < parts.length; i++) {
            String newSuggestion;
            String part = parts[i];
            if (suggestion.length() > 0) {
                newSuggestion = suggestion + "-" + part;
            } else {
                newSuggestion = part;
            }
            
            if (newSuggestion.length() > 20) {
                if (i == 0) {
                    return newSuggestion.substring(0, 20);
                }
                
                return suggestion;
            }
            
            suggestion = newSuggestion;
        }
        
        return suggestion;
    }
    
    public static String confirmHandle(String suggestedHandle, List<String> similarHandles) {
        if (similarHandles.contains(suggestedHandle)) {
    
            int ordinal = 2;
            int suggestedHandleLength = suggestedHandle.length();
            List<String> trimmedSimilarHandles = similarHandles.stream().map(similarHandle -> similarHandle.substring(suggestedHandleLength)).collect(Collectors.toList());
            for (String trimmedSimilarHandle : trimmedSimilarHandles) {
                if (trimmedSimilarHandle.startsWith("-")) {
                    String[] parts = trimmedSimilarHandle.replaceFirst("-", "").split("-");
                    String firstPart = parts[0];
                    
                    // We only care about creating a unique value in a formatted sequence
                    // We can ignore anything else that has been reformatted by an end user
                    if (parts.length == 1 && StringUtils.isNumeric(firstPart)) {
                        ordinal = Integer.parseInt(firstPart) + 1;
                        break;
                    }
                }
            }
            
            return suggestedHandle + "-" + ordinal;
        }
        
        return suggestedHandle;
    }
    
    public ResourceOperation createResourceOperation(Resource resource, Action action, User user) {
        ResourceOperation resourceOperation = new ResourceOperation().setResource(resource).setAction(action).setUser(user).setComment(resource.getComment());
        
        ResourceChangeListRepresentation changeList = resource.getChangeList();
        if (changeList != null) {
            try {
                resourceOperation.setChangeList(objectMapper.writeValueAsString(changeList));
            } catch (JsonProcessingException e) {
                LOGGER.info("Could not serialize change list", e);
            }
        }
        
        resourceOperation = resourceOperationRepository.save(resourceOperation);
        resource.getOperations().add(resourceOperation);
        return resourceOperation;
    }
    
    public List<String> getCategories(Resource resource, CategoryType categoryType) {
        List<ResourceCategory> resourceCategories = resource.getCategories().stream()
            .filter(resourceCategory -> BooleanUtils.isTrue(resourceCategory.getActive()))
            .filter(resourceCategory -> categoryType == resourceCategory.getType())
            .collect(Collectors.toList());
        
        if (resourceCategories.isEmpty()) {
            return null;
        }
        
        return resourceCategories.stream().map(ResourceCategory::getName).collect(Collectors.toList());
    }
    
    @SuppressWarnings("JpaQlInspection")
    public List<ResourceOperation> getResourceOperations(Long id) {
        return new ArrayList<>(entityManager.createQuery(
            "select resourceOperation " +
                "from ResourceOperation resourceOperation " +
                "where resourceOperation.resource.id = :resourceId " +
                "order by resourceOperation.id desc", ResourceOperation.class)
            .setParameter("resourceId", id)
            .setHint("javax.persistence.loadgraph", "resource.operation")
            .getResultList());
    }
    
    void updateResource(Resource resource, LocalDateTime baseline) {
        resourceRepository.update(resource, baseline);
    }
    
    private void commitResourceRelation(Resource resource1, Resource resource2) {
        ResourceRelation resourceRelation = new ResourceRelation().setResource1(resource1).setResource2(resource2);
        resourceRelationRepository.save(resourceRelation);
    
        resource1.getChildren().add(resourceRelation);
        resource2.getParents().add(resourceRelation);
    }
    
    @SuppressWarnings("unchecked")
    private List<Object[]> getResources(String statement, List<String> filterStatements, Map<String, String> filterParameters) {
        Query query = entityManager.createNativeQuery(Joiner.on(" where ").skipNulls().join(statement, Joiner.on(" and ").join(filterStatements)));
        filterParameters.keySet().forEach(key -> query.setParameter(key, filterParameters.get(key)));
        return query.getResultList();
    }
    
    private static class ResourceActionKey {
        
        private Long id;
        
        private Action action;
        
        private Scope scope;
        
        public ResourceActionKey(Long id, Action action, Scope scope) {
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
