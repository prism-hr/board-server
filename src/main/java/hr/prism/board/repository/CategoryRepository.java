package hr.prism.board.repository;

import hr.prism.board.domain.Category;
import hr.prism.board.domain.Resource;
import hr.prism.board.enums.CategoryType;

import java.util.List;

public interface CategoryRepository extends MyRepository<Category, Long> {

    List<Category> findByParentResourceAndTypeAndNameIn(Resource resource, CategoryType type, List<String> names);

}
