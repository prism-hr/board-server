package hr.prism.board.service;

import hr.prism.board.domain.Document;
import hr.prism.board.dto.DocumentDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DocumentService {

    public DocumentDTO mapDocument(Document document) {
        return new DocumentDTO()
                .withCloudinaryId(document.getCloudinaryId())
                .withCloudinaryUrl(document.getCloudinaryUrl())
                .withFileName(document.getFileName());
    }

}
