package hr.prism.board.mapper;

import hr.prism.board.domain.Document;
import hr.prism.board.representation.DocumentRepresentation;
import hr.prism.board.value.ResourceSearch;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class DocumentMapper implements Function<Document, DocumentRepresentation> {

    @Override
    public DocumentRepresentation apply(Document document) {
        if (document == null) {
            return null;
        }

        return new DocumentRepresentation()
            .setId(document.getId())
            .setCloudinaryId(document.getCloudinaryId())
            .setCloudinaryUrl(document.getCloudinaryUrl())
            .setFileName(document.getFileName());
    }

    public DocumentRepresentation apply(ResourceSearch document) {
        if (document == null) {
            return null;
        }

        String documentImageCloudinaryId = document.getDocumentLogoCloudinaryId();
        if (documentImageCloudinaryId == null) {
            return null;
        }

        return new DocumentRepresentation()
            .setCloudinaryId(documentImageCloudinaryId)
            .setCloudinaryUrl(document.getDocumentLogoCloudinaryUrl())
            .setFileName(document.getDocumentLogoFileName());
    }

}
