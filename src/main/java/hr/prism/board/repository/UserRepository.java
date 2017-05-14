package hr.prism.board.repository;

import hr.prism.board.domain.User;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

@SuppressWarnings("JpaQlInspection")
public interface UserRepository extends MyRepository<User, Long> {
    
    User findByEmail(String email);
    
    @Query(value =
        "select user " +
            "from User user " +
            "where user.email = :email " +
            "and user.password = :password " +
            "or (user.temporaryPassword = :password " +
            "and user.temporaryPasswordExpiryTimestamp <= :baseline)")
    User findByEmailAndPassword(String email, String password, LocalDateTime baseline);
    
}
