package hr.prism.board.repository;

import hr.prism.board.domain.Location;

public interface LocationRepository extends MyRepository<Location, Long> {

    Location findByGoogleId(String googleId);

}
