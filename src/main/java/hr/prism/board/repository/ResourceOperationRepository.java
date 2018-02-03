package hr.prism.board.repository;

import hr.prism.board.domain.Resource;
import hr.prism.board.domain.ResourceOperation;
import hr.prism.board.enums.Action;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface ResourceOperationRepository extends BoardEntityRepository<ResourceOperation, Long> {

    ResourceOperation findFirstByResourceAndActionOrderByIdDesc(Resource resource, Action action);

}
