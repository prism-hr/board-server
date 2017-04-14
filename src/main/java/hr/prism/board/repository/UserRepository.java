package hr.prism.board.repository;

import hr.prism.board.domain.User;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends MyRepository<User, Long> {
    
    User findByStormpathId(String stormpathId);
    
    User findByEmail(String email);
    
    @Query(value =
        "select user " +
            "from User user " +
            "where user.stormpathId is null")
    User findByStormpathIdNull();
    
}
