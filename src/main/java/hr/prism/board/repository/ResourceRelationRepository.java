package hr.prism.board.repository;

import hr.prism.board.domain.ResourceRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface ResourceRelationRepository extends JpaRepository<ResourceRelation, Long> {

}
