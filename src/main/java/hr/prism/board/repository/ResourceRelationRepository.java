package hr.prism.board.repository;

import hr.prism.board.domain.Resource;
import hr.prism.board.domain.ResourceRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface ResourceRelationRepository extends JpaRepository<ResourceRelation, Long> {

    List<ResourceRelation> findByResource2(Resource resource2);

}
