package hr.prism.board.validation;

import com.google.common.annotations.VisibleForTesting;
import hr.prism.board.domain.Post;
import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.ExceptionCode;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static hr.prism.board.enums.State.DRAFT;
import static hr.prism.board.exception.ExceptionCode.*;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

@Component
public class PostValidator {

    public void checkApply(Post post) {
        long applyCount =
            Stream.of(post.getApplyWebsite(), post.getApplyDocument(), post.getApplyEmail())
                .filter(Objects::nonNull)
                .count();

        if (applyCount == 0) {
            throw new BoardException(MISSING_APPLY_OPTION, "No apply mechanism specified");
        } else if (applyCount > 1) {
            throw new BoardException(CORRUPTED_APPLY_OPTION, "Multiple apply mechanisms specified");
        }

        checkApplyWebsite(post);
    }

    public void checkCategories(List<String> categories, List<String> permittedCategories,
                                 ExceptionCode exceptionCodeWhenForbidden, ExceptionCode exceptionCodeWhenMissing,
                                 ExceptionCode exceptionCodeWhenInvalid) {
        if (permittedCategories.isEmpty()) {
            if (isNotEmpty(categories)) {
                throw new BoardException(exceptionCodeWhenForbidden, "Categories must not be specified");
            }
        } else {
            if (isEmpty(categories)) {
                throw new BoardException(exceptionCodeWhenMissing, "Categories must be specified");
            }

            if (!permittedCategories.containsAll(categories)) {
                throw new BoardException(exceptionCodeWhenInvalid,
                    "Valid categories must be specified - check parent categories");
            }
        }
    }

    public void checkExistingRelation(Post post) {
        if (post.getState() == DRAFT && post.getExistingRelation() == null) {
            throw new BoardException(MISSING_EXISTING_RELATION, "Existing relation explanation required");
        }
    }

    @VisibleForTesting
    @SuppressWarnings("WeakerAccess")
    public void checkApplyWebsite(Post post) {
        String applyWebsite = post.getApplyWebsite();
        if (applyWebsite == null) {
            return;
        }

        try {
            URL applyUrl = new URL(applyWebsite);
            HttpURLConnection connection = (HttpURLConnection) applyUrl.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setInstanceFollowRedirects(true);
            connection.connect();

            int responseCode = connection.getResponseCode();
            if (responseCode < 200 || responseCode >= 400) {
                throw new BoardException(INACCESSIBLE_APPLY_WEBSITE, "Cannot access apply website");
            }
        } catch (IOException e) {
            throw new BoardException(INACCESSIBLE_APPLY_WEBSITE, "Cannot access apply website");
        }
    }

}
