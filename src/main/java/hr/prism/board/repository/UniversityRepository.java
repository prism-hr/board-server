package hr.prism.board.repository;

import hr.prism.board.domain.University;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@SuppressWarnings("JpaQlInspection")
public interface UniversityRepository extends MyRepository<University, Long> {

    @Query(value =
        "select university " +
            "from University university " +
            "where university.name = :name " +
            "or university.handle = :handle")
    University findByNameOrHandle(@Param("name") String name, @Param("handle") String handle);

}
