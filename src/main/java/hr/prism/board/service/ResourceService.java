package hr.prism.board.service;

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import hr.prism.board.domain.*;
import hr.prism.board.dto.ResourceFilter;
import hr.prism.board.dto.ResourceFilterDTO;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.CategoryType;
import hr.prism.board.enums.State;
import hr.prism.board.repository.CategoryRepository;
import hr.prism.board.repository.ResourceRelationRepository;
import hr.prism.board.repository.ResourceRepository;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.inject.Inject;
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
    
    private static String[] resourceActionStatements = new String[]{
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
            "and permission.role = user_role.role #" +
            "where user_role.user_id = :userId",
        "select resource.id, permission.action, " +
            "permission.resource3_scope, permission.resource3_state " +
            "from resource " +
            "inner join permission " +
            "on resource.scope = permission.resource2_scope " +
            "and resource.state = permission.resource2_state " +
            "where permission.role = 'PUBLIC'"};
    
    @Inject
    private ResourceRepository resourceRepository;
    
    @Inject
    private ResourceRelationRepository resourceRelationRepository;
    
    @Inject
    private CategoryRepository categoryRepository;
    
    @Inject
    private DepartmentService departmentService;
    
    @Inject
    private BoardService boardService;
    
    @Inject
    private PostService postService;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Inject
    private PlatformTransactionManager platformTransactionManager;
    
    public Resource findOne(Long id) {
        return resourceRepository.findOne(id);
    }
    
    public Resource findByHandle(String handle) {
        return resourceRepository.findByHandle(handle);
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
            existingResourceCategory.setUpdatedTimestamp(LocalDateTime.now());
        }
    
        // add new categories
        for (String postedCategory : postedCategories) {
            ResourceCategory newResourceCategory = new ResourceCategory().setResource(resource).setName(postedCategory).setActive(true).setType(type);
            newResourceCategory.setCreatedTimestamp(LocalDateTime.now());
            categoryRepository.save(newResourceCategory);
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
    
    public HashMultimap<Resource, ResourceAction> getResourceActions(ResourceFilterDTO filter) throws IllegalAccessException {
        entityManager.flush();
        List<String> filterStatements = new ArrayList<>();
        Map<String, Object> filterParameters = new HashMap<>();
        for (Field field : ResourceFilterDTO.class.getDeclaredFields()) {
            Object value = field.get(filter);
            if (value != null) {
                ResourceFilter resourceFilter = field.getAnnotation(ResourceFilter.class);
                if (resourceFilter != null) {
                    String placeholder = resourceFilter.placeholder();
                    filterStatements.add(resourceFilter.column() + " = " + placeholder);
                    filterParameters.put(placeholder, value);
                }
            }
        }
        
        TransactionTemplate transactionTemplate = new TransactionTemplate(platformTransactionManager);
        List<Object[]> rows = transactionTemplate.execute(status -> {
            List<Object[]> results = Lists.newArrayList();
            for (String statement : resourceActionStatements) {
                Query query = entityManager.createNativeQuery(statement + " WHERE " + Joiner.on(" AND ").join(filterStatements));
                filterParameters.keySet().forEach(key -> query.setParameter(key, filterParameters.get(key)));
                query.getResultList().forEach(row -> results.add((Object[]) row));
            }
            
            return results;
        });
        
        Map<List<Object>, ResourceAction> rowIndex = new HashMap<>();
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
            
            // Use the mapping that provides the most expedient state transition, this can vary by role
            List<Object> rowKey = Arrays.asList(rowId, rowAction, rowScope);
            ResourceAction rowValue = rowIndex.get(rowKey);
            if (rowValue == null || ObjectUtils.compare(rowState, rowValue.getState()) > 0) {
                rowIndex.put(rowKey, new ResourceAction().setAction(rowAction).setScope(rowScope).setState(rowState));
            }
        }
        
        if (rowIndex.isEmpty()) {
            return HashMultimap.create();
        }
        
        Map<Long, Resource> resources = null;
        List<Long> ids = rowIndex.keySet().stream().map(key -> (Long) key.get(0)).collect(Collectors.toList());
        switch (filter.getScope()) {
            case DEPARTMENT:
                resources = departmentService.findByIdIn(ids).stream().collect(Collectors.toMap(Resource::getId, resource -> resource));
                break;
            case BOARD:
                resources = boardService.findByIdIn(ids).stream().collect(Collectors.toMap(Resource::getId, resource -> resource));
                break;
            case POST:
                resources = postService.findByIdIn(ids).stream().collect(Collectors.toMap(Resource::getId, resource -> resource));
                break;
        }
        
        HashMultimap<Resource, ResourceAction> resourceActions = HashMultimap.create();
        for (List<Object> key : rowIndex.keySet()) {
            ResourceAction resourceAction = rowIndex.get(key);
            resourceActions.put(resources.get(key.get(0)), resourceAction);
        }
        
        return resourceActions;
    }
    
    private void commitResourceRelation(Resource resource1, Resource resource2) {
        ResourceRelation resourceRelation = new ResourceRelation().setResource1(resource1).setResource2(resource2);
        resourceRelationRepository.save(resourceRelation);
        
        resource1.getChildren().add(resourceRelation);
        resource2.getParents().add(resourceRelation);
    }
    
}
