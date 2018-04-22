package hr.prism.board.repository;

import hr.prism.board.domain.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface DocumentRepository extends JpaRepository<Document, Long> {

}
