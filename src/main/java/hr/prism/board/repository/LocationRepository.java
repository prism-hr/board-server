package hr.prism.board.repository;

import hr.prism.board.domain.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface LocationRepository extends JpaRepository<Location, Long> {

    Location findByGoogleId(String googleId);

}
