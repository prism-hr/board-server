package hr.prism.board.repository;

import hr.prism.board.domain.UserSearch;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;

public interface UserSearchRepository extends MyRepository<UserSearch, Long> {

    @Modifying
    @Query(value =
        "INSERT INTO user_search(user_id, search) " +
            "SELECT user.id , :search " +
            "MATCH USER.indexData against(:searchTerm IN BOOLEAN MODE) AS similarity " +
            "FROM USER " +
            "WHERE USER.id IN (:userIds) " +
            "HAVING SIMILARITY > 0 " +
            "ORDER BY similarity DESC, USER.id DESC",
        nativeQuery = true)
    void insertBySearch(@Param("search") String search, @Param("searchTerm") String searchTerm, @Param("userIds") Collection<Long> userIds);

    void deleteBySearch(String search);

}
