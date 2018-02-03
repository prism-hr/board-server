package hr.prism.board.repository;

import hr.prism.board.domain.Resource;
import hr.prism.board.domain.ResourceRelation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface ResourceRelationRepository extends BoardEntityRepository<ResourceRelation, Long> {

    List<ResourceRelation> findByResource2(Resource resource2);

}
