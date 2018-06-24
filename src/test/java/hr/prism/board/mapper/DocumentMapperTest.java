package hr.prism.board.mapper;

import hr.prism.board.domain.Document;
import hr.prism.board.representation.DocumentRepresentation;
import hr.prism.board.value.DocumentSearch;
import hr.prism.board.value.ResourceSearch;
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

    @Test
    public void apply_resourceSearch_success() {
        DocumentSearch resourceSearch = new DocumentSearch(
            "cloudinaryId", "cloudinaryUrl", "fileName");

        DocumentRepresentation documentRepresentation = documentMapper.apply(resourceSearch);

        assertEquals("cloudinaryId", documentRepresentation.getCloudinaryId());
        assertEquals("cloudinaryUrl", documentRepresentation.getCloudinaryUrl());
        assertEquals("fileName", documentRepresentation.getFileName());
    }

    @Test
    public void apply_resourceSearch_successWhenNull() {
        assertNull(documentMapper.apply((ResourceSearch) null));
    }

    @Test
    public void apply_resourceSearch_successWhenCloudinaryIdNull() {
        assertNull(documentMapper.apply(new ResourceSearch(1L,
            "resource", null, null, null)));
    }

}
