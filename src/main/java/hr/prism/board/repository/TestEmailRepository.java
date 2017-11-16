package hr.prism.board.repository;

import hr.prism.board.domain.TestEmail;
import hr.prism.board.representation.TestEmailMessageRepresentation;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

@SuppressWarnings("JpaQlInspection")
public interface TestEmailRepository extends MyRepository<TestEmail, Long> {

    @Query(value =
        "select testEmail.message " +
            "from TestEmail testEmail " +
            "order by testEmail.id")
    List<TestEmailMessageRepresentation> findAllMessages();

    @Query(value =
        "select testEmail.message " +
            "from TestEmail testEmail " +
            "where testEmail.user.id = :userId " +
            "order by testEmail.id")
    List<TestEmailMessageRepresentation> findMessagesByUserId(Long userId);

}
