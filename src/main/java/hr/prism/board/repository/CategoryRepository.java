package hr.prism.board.repository;

import hr.prism.board.domain.Resource;
import hr.prism.board.domain.ResourceCategory;
import hr.prism.board.enums.CategoryType;

import java.util.List;

public interface CategoryRepository extends MyRepository<ResourceCategory, Long> {
    
    List<ResourceCategory> findByParentResourceAndTypeAndNameIn(Resource resource, CategoryType type, List<String> names);

}
