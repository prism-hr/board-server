package hr.prism.board.service;

import hr.prism.board.DbTestContext;
import hr.prism.board.domain.Document;
import hr.prism.board.dto.DocumentDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

@DbTestContext
@RunWith(SpringRunner.class)
@Sql("classpath:data/documentService_setUp.sql")
@Sql(value = "classpath:data/documentService_tearDown.sql", executionPhase = AFTER_TEST_METHOD)
public class DocumentServiceIT {

    @Inject
    private DocumentService documentService;

    @Inject
    private ServiceHelper serviceHelper;

    @Test
    public void getOrCreateDocument_successWhenExisting() {
        Document document = documentService.getOrCreateDocument(new DocumentDTO().setId(1L));
        verifyDocument(document,
            "cloudinary id", "cloudinary url", "file name");
    }

    @Test
    public void getOrCreateDocument_successWhenNew() {
        LocalDateTime baseline = LocalDateTime.now();
        DocumentDTO documentDTO =
            new DocumentDTO()
                .setCloudinaryId("new cloudinary id")
                .setCloudinaryUrl("new cloudinary url")
                .setFileName("new file name");

        Document createdDocument = documentService.getOrCreateDocument(documentDTO);
        verifyDocument(createdDocument, "new cloudinary id",
            "new cloudinary url", "new file name");
        serviceHelper.verifyTimestamps(createdDocument, baseline);

        Document selectedDocument =
            documentService.getOrCreateDocument(new DocumentDTO().setId(createdDocument.getId()));
        verifyDocument(selectedDocument, "new cloudinary id",
            "new cloudinary url", "new file name");
        assertEquals(createdDocument, selectedDocument);
    }

    private void verifyDocument(Document document, String expectedCloudinaryId, String expectedCloudinaryUrl,
                                String expectedFileName) {
        assertNotNull(document.getId());
        assertEquals(expectedCloudinaryId, document.getCloudinaryId());
        assertEquals(expectedCloudinaryUrl, document.getCloudinaryUrl());
        assertEquals(expectedFileName, document.getFileName());
    }

}
