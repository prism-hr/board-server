package hr.prism.board.repository;

import hr.prism.board.domain.UserSearch;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;

public interface UserSearchRepository extends MyRepository<UserSearch, Long> {

    @Modifying
    @Query(value =
        "INSERT INTO user_search(user_id, search) " +
            "SELECT user.id , :search " +
            "MATCH user.indexData against(:searchTerm IN BOOLEAN MODE) AS similarity " +
            "FROM user " +
            "WHERE user.id IN (:userIds) " +
            "HAVING SIMILARITY > 0 " +
            "ORDER BY similarity DESC, user.id DESC",
        nativeQuery = true)
    void insertBySearch(String search, String searchTerm, Collection<Long> userIds);

    void deleteBySearch(String search);

}
