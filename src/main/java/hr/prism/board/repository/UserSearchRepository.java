package hr.prism.board.repository;

import hr.prism.board.domain.UserSearch;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;

@Transactional
public interface UserSearchRepository extends SearchRepository<UserSearch> {

    @Modifying
    @Query(value =
        "INSERT INTO user_search (user_id, search, creator_id, created_timestamp, updated_timestamp) " +
            "SELECT user_search_result.user_id, user_search_result.search, user_search_result.creator_id, :baseline, " +
            ":baseline " +
            "FROM (" +
            "SELECT user.id as user_id, :search as search, user.creator_id as creator_id, " +
            "MATCH(user.index_data) AGAINST(:searchTerm IN BOOLEAN MODE) AS similarity " +
            "FROM user " +
            "WHERE user.id IN (:userIds) " +
            "HAVING similarity > 0 " +
            "ORDER BY similarity DESC, user.id DESC) AS user_search_result",
        nativeQuery = true)
    void insertBySearch(@Param("search") String search, @Param("baseline") LocalDateTime localDateTime,
                        @Param("searchTerm") String searchTerm, @Param("userIds") Collection<Long> userIds);

}
