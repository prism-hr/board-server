package hr.prism.board.notification.property;

import hr.prism.board.domain.Post;
import hr.prism.board.service.NotificationService;
import hr.prism.board.service.PostService;
import hr.prism.board.utils.BoardUtils;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class PublicationScheduleProperty implements NotificationProperty {

    @Inject
    private PostService postService;

    public String getValue(NotificationService.NotificationRequest notificationRequest) {
        Post post = (Post) notificationRequest.getResource();
        if (post.getLiveTimestamp() == null) {
            return "imminently. We will send you a follow-up message when your post has gone live";
        }

        String liveTimestamp = postService.getEffectiveLiveTimestamp(post).format(BoardUtils.DATETIME_FORMATTER);
        return "on or around " + liveTimestamp + ". We will send you a follow-up message when your post has gone live";
    }

}
