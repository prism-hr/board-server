package hr.prism.board.repository;

import hr.prism.board.domain.Resource;
import hr.prism.board.domain.ResourceOperation;
import hr.prism.board.enums.Action;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@SuppressWarnings("JpaQlInspection")
public interface ResourceOperationRepository extends MyRepository<ResourceOperation, Long> {

    @Query(value =
        "select resourceOperation " +
            "from ResourceOperation resourceOperation " +
            "where resourceOperation.resource = :resource " +
            "and resourceOperation.action in (:actions) " +
            "order by resourceOperation.id desc")
    List<ResourceOperation> findFirstByResourceAndActionOrderByIdDesc(@Param("resource") Resource resource, @Param("actions") Action[] actions);

}
