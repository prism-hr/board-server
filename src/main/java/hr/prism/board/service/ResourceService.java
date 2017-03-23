package hr.prism.board.service;

import com.google.common.base.Joiner;
import hr.prism.board.domain.Category;
import hr.prism.board.domain.Resource;
import hr.prism.board.domain.ResourceRelation;
import hr.prism.board.enums.CategoryType;
import hr.prism.board.repository.CategoryRepository;
import hr.prism.board.repository.ResourceRelationRepository;
import hr.prism.board.repository.ResourceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class ResourceService {

    @Inject
    private ResourceRepository resourceRepository;

    @Inject
    private ResourceRelationRepository resourceRelationRepository;

    @Inject
    private CategoryRepository categoryRepository;

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
        if (resource1.getScope().ordinal() == (resource2.getScope().ordinal() - 1)) {
            commitResourceRelation(resource2, resource2);
            resource1.getParents().stream().map(ResourceRelation::getResource1).forEach(parentResource -> commitResourceRelation(parentResource, resource2));
            return;
        }
        throw new IllegalStateException("Incorrect use of method. First argument must be of direct parent scope of second argument. Arguments passed where: " +
            Joiner.on(", ").join(resource1, resource2));
    }
    
    public void updateCategories(Resource resource, List<String> categories, CategoryType type) {
        HashSet<String> postedCategories = new HashSet<>(categories);
        Set<Category> existingCategories = resource.getCategories();

        // modify existing categories
        for (Category existingCategory : existingCategories) {
            if (postedCategories.remove(existingCategory.getName())) {
                existingCategory.setActive(true);
            } else {
                existingCategory.setActive(false); // category was not in the posted list, make inactive
            }
            existingCategory.setUpdatedTimestamp(LocalDateTime.now());
        }

        // add new categories
        for (String postedCategory : postedCategories) {
            Category newCategory = new Category().setParentResource(resource).setName(postedCategory).setActive(true).setType(type);
            newCategory.setCreatedTimestamp(LocalDateTime.now());
            categoryRepository.save(newCategory);
            existingCategories.add(newCategory);
        }
    }
    
    private void commitResourceRelation(Resource resource1, Resource resource2) {
        ResourceRelation resourceRelation = new ResourceRelation().setResource1(resource1).setResource2(resource2);
        resourceRelationRepository.save(resourceRelation);
        
        resource1.getChildren().add(resourceRelation);
        resource2.getParents().add(resourceRelation);
    }
    
}
