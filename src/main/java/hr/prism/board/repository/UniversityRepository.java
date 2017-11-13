package hr.prism.board.repository;

import hr.prism.board.domain.University;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@SuppressWarnings("JpaQlInspection")
public interface UniversityRepository extends MyRepository<University, Long> {

    @Query(value =
        "select university.id " +
            "from University university " +
            "order by university.name")
    List<Long> findAllIds();

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

    @Query(value =
        "select university.handle " +
            "from University university " +
            "where university.handle like concat('%', :suggestedHandle, '-%') " +
            "order by university.handle desc")
    List<String> findHandleByLikeSuggestedHandle(@Param("suggestedHandle") String suggestedHandle);

}
