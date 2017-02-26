package hr.prism.board.service;

import hr.prism.board.domain.Document;
import hr.prism.board.representation.DocumentRepresentation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DocumentService {

    public DocumentRepresentation mapDocument(Document document) {
        if(document == null) {
            return null;
        }
        return new DocumentRepresentation()
                .withCloudinaryId(document.getCloudinaryId())
                .withCloudinaryUrl(document.getCloudinaryUrl())
                .withFileName(document.getFileName());
    }

}
