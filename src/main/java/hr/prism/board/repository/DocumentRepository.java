package hr.prism.board.repository;

import hr.prism.board.domain.Document;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface DocumentRepository extends BoardEntityRepository<Document, Long> {

    Document findByCloudinaryId(String cloudinaryId);

}
