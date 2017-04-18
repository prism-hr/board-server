package hr.prism.board.repository;

import hr.prism.board.domain.Resource;
import hr.prism.board.domain.ResourceCategory;
import hr.prism.board.enums.CategoryType;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@SuppressWarnings("JpaQlInspection")
public interface ResourceCategoryRepository extends MyRepository<ResourceCategory, Long> {
    
    List<ResourceCategory> findByResourceAndTypeAndNameIn(Resource resource, CategoryType type, List<String> names);
    
    @Modifying
    @Query(value =
        "delete from ResourceCategory resourceCategory " +
            "where resourceCategory.resource = :resource " +
            "and resourceCategory.type = :type")
    void deleteByResourceAndType(@Param("resource") Resource resource, @Param("type") CategoryType type);
    
}
