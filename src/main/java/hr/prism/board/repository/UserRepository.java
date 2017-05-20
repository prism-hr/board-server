package hr.prism.board.repository;

import hr.prism.board.domain.User;
import hr.prism.board.enums.OauthProvider;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

@SuppressWarnings("JpaQlInspection")
public interface UserRepository extends MyRepository<User, Long> {
    
    User findByEmail(String email);
    
    User findByOauthProviderAndOauthAccountId(OauthProvider provider, String oauthAccountId);
    
    @Query(value =
        "select user " +
            "from User user " +
            "where user.email = :email " +
            "and (user.password = :password " +
            "or (user.temporaryPassword = :password " +
            "and user.temporaryPasswordExpiryTimestamp <= :baseline))")
    User findByEmailAndPassword(@Param("email") String email, @Param("password") String password, @Param("baseline") LocalDateTime baseline);
    
}
