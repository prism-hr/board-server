package hr.prism.board.repository;

import hr.prism.board.domain.Location;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface LocationRepository extends BoardEntityRepository<Location, Long> {

    Location findByGoogleId(String googleId);

}
