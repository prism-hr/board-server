package hr.prism.board.mapper;

import hr.prism.board.domain.Document;
import hr.prism.board.representation.DocumentRepresentation;
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

}
