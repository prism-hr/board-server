package hr.prism.board.mapper;

import hr.prism.board.domain.Document;
import hr.prism.board.representation.DocumentRepresentation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(MockitoJUnitRunner.class)
public class DocumentMapperTest {

    private DocumentMapper documentMapper = new DocumentMapper();

    @Test
    public void apply_success() {
        Document document = new Document();
        document.setId(1L);
        document.setCloudinaryId("cloudinaryId");
        document.setCloudinaryUrl("cloudinaryUrl");
        document.setFileName("fileName");

        DocumentRepresentation documentRepresentation = documentMapper.apply(document);
        assertEquals(1L, documentRepresentation.getId().longValue());
        assertEquals("cloudinaryId", documentRepresentation.getCloudinaryId());
        assertEquals("cloudinaryUrl", documentRepresentation.getCloudinaryUrl());
        assertEquals("fileName", documentRepresentation.getFileName());
    }

    @Test
    public void apply_successWhenNull() {
        assertNull(documentMapper.apply((Document) null));
    }


}
