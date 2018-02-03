package hr.prism.board.repository;

import hr.prism.board.domain.University;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@SuppressWarnings("JpaQlInspection")
public interface UniversityRepository extends BoardEntityRepository<University, Long> {

    @Query(value =
        "select university " +
            "from University university " +
            "order by university.name")
    List<University> findAll();

    @Query(value =
        "select university " +
            "from University university " +
            "where university.name = :name " +
            "or university.handle = :handle")
    University findByNameOrHandle(@Param("name") String name, @Param("handle") String handle);

}
