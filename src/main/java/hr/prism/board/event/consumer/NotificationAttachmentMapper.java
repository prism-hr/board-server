package hr.prism.board.event.consumer;

import hr.prism.board.exception.BoardException;
import hr.prism.board.notification.BoardAttachments;
import hr.prism.board.workflow.Notification.Attachment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.function.Function;

import static hr.prism.board.exception.ExceptionCode.CONNECTION_ERROR;
import static java.util.Base64.getEncoder;
import static org.apache.commons.io.IOUtils.toByteArray;

@Component
public class NotificationAttachmentMapper implements Function<Attachment, BoardAttachments> {

    @Override
    public BoardAttachments apply(Attachment attachment) {
        try {
            String urlPath = attachment.getUrl();
            URL url = new URL(urlPath);
            URLConnection connection = url.openConnection();
            try (InputStream inputStream = connection.getInputStream()) {
                BoardAttachments attachments = new BoardAttachments();
                attachments.setContent(getEncoder().encodeToString(toByteArray(inputStream)));
                attachments.setType(connection.getContentType());
                attachments.setFilename(attachment.getName());
                attachments.setDisposition("attachment");
                attachments.setContentId(attachment.getLabel());
                attachment.setUrl(urlPath);
                return attachments;
            } catch (IOException e) {
                throw new BoardException(CONNECTION_ERROR, "Could not retrieve attachment data");
            }
        } catch (IOException e) {
            throw new BoardException(CONNECTION_ERROR, "Could not access attachment data");
        }
    }

}
