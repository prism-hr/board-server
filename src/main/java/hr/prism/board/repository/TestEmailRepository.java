package hr.prism.board.repository;

import hr.prism.board.domain.TestEmail;
import hr.prism.board.representation.TestEmailMessageRepresentation;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface TestEmailRepository extends BoardEntityRepository<TestEmail, Long> {

    @Query(value =
        "select testEmail.message " +
            "from TestEmail testEmail " +
            "order by testEmail.id desc")
    List<TestEmailMessageRepresentation> findAllMessages();

    @Query(value =
        "select testEmail.message " +
            "from TestEmail testEmail " +
            "where testEmail.email = :email " +
            "order by testEmail.id desc")
    List<TestEmailMessageRepresentation> findMessagesByUserEmail(@Param("email") String email);

}
