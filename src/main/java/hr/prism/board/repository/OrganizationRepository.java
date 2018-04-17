package hr.prism.board.repository;

import hr.prism.board.domain.Organization;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface OrganizationRepository extends BoardEntityRepository<Organization, Long> {

    Organization findByName(String name);

}
