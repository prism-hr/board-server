package hr.prism.board.validation;

import hr.prism.board.exception.BoardException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import static hr.prism.board.exception.ExceptionCode.INACCESSIBLE_POST_APPLY;

@Component
public class UrlValidator {

    public void checkPathIsUrl(String path) {
        try {
            URL url = new URL(path);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setInstanceFollowRedirects(true);
            connection.connect();

            int responseCode = connection.getResponseCode();
            if (responseCode < 200 || responseCode >= 400) {
                throw new BoardException(INACCESSIBLE_POST_APPLY, "Cannot access apply website");
            }
        } catch (IOException e) {
            throw new BoardException(INACCESSIBLE_POST_APPLY, "Cannot access apply website");
        }
    }

}
