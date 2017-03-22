package hr.prism.board.service;

import hr.prism.board.domain.*;
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
        if (resource1.getScope() == Scope.BOARD && resource2.getScope() == Scope.DEPARTMENT) {
            throw new IllegalStateException("A board cannot be the parent of a department");
        }

        ResourceRelation resourceRelation = new ResourceRelation().setResource1(resource1).setResource2(resource2);
        resourceRelationRepository.save(resourceRelation);

        resource1.getChildren().add(resourceRelation);
        resource2.getParents().add(resourceRelation);
    }

    public void updateParentCategories(Resource parentResource, List<String> categories, CategoryType type) {
        HashSet<String> postedCategories = new HashSet<>(categories);
        Set<Category> existingCategories = parentResource.getCategories();

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
            Category newCategory = new Category().setParentResource(parentResource).setName(postedCategory).setActive(true).setType(type);
            newCategory.setCreatedTimestamp(LocalDateTime.now());
            categoryRepository.save(newCategory);
            existingCategories.add(newCategory);
        }
    }
}
