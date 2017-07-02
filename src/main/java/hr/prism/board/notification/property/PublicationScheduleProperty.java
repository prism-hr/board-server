package hr.prism.board.notification.property;

import hr.prism.board.domain.Post;
import hr.prism.board.enums.State;
import hr.prism.board.service.NotificationService;
import hr.prism.board.service.PostService;
import hr.prism.board.util.BoardUtils;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class PublicationScheduleProperty implements NotificationProperty {

    @Inject
    private PostService postService;

    public String getValue(NotificationService.NotificationInstance notificationInstance) {
        Post post = (Post) notificationInstance.getResource();
        if (post.getState() == State.ACCEPTED) {
            return "immediately";
        }

        String liveTimestamp = postService.getEffectiveLiveTimestamp(post).format(BoardUtils.DATETIME_FORMATTER);
        return "on or around " + liveTimestamp + ". We will send you a follow-up message when your post has gone live";
    }

}
