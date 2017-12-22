package hr.prism.board.repository;

import hr.prism.board.domain.Document;

public interface DocumentRepository extends BoardEntityRepository<Document, Long> {

    Document findByCloudinaryId(String cloudinaryId);

}
