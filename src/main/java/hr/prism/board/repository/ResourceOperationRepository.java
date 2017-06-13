package hr.prism.board.repository;

import hr.prism.board.domain.ResourceOperation;
import hr.prism.board.enums.Action;

public interface ResourceOperationRepository extends MyRepository<ResourceOperation, Long> {

    ResourceOperation findFirstByActionByOrderByIdDesc(Action action);

}
