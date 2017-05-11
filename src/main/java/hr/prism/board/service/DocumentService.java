package hr.prism.board.service;

import hr.prism.board.domain.Document;
import hr.prism.board.dto.DocumentDTO;
import hr.prism.board.repository.DocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

@Service
@Transactional
public class DocumentService {

    @Inject
    private DocumentRepository documentRepository;


    public Document getOrCreateDocument(DocumentDTO documentDTO) {
        if (documentDTO == null) {
            return null;
        }

        if (documentDTO.getId() != null) {
            return documentRepository.findOne(documentDTO.getId());
        }

        Document document = new Document();
        document.setFileName(documentDTO.getFileName());
        document.setCloudinaryId(documentDTO.getCloudinaryId());
        document.setCloudinaryUrl(documentDTO.getCloudinaryUrl());
        document = documentRepository.save(document);
        return document;
    }

    public void deleteDocument(Document document) {
        documentRepository.delete(document);
    }

}
