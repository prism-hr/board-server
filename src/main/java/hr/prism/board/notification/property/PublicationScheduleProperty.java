package hr.prism.board.notification.property;

import hr.prism.board.domain.Post;
import hr.prism.board.service.NotificationService.NotificationRequest;
import hr.prism.board.service.PostService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

import static hr.prism.board.utils.BoardUtils.DATETIME_FORMATTER;

@Component
public class PublicationScheduleProperty implements NotificationProperty {

    private final PostService postService;

    @Inject
    public PublicationScheduleProperty(PostService postService) {
        this.postService = postService;
    }

    public String getValue(NotificationRequest notificationRequest) {
        Post post = (Post) notificationRequest.getResource();
        if (post.getLiveTimestamp() == null) {
            return "imminently. We will send you a follow-up message when your post has gone live";
        }

        String liveTimestamp = postService.getEffectiveLiveTimestamp(post).format(DATETIME_FORMATTER);
        return "on or around " + liveTimestamp + ". We will send you a follow-up message when your post has gone live";
    }

}
