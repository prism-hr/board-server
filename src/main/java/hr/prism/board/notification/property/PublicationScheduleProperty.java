package hr.prism.board.notification.property;

import hr.prism.board.domain.Post;
import hr.prism.board.domain.Resource;
import hr.prism.board.enums.Action;
import hr.prism.board.enums.State;
import hr.prism.board.service.PostService;
import hr.prism.board.util.BoardUtils;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class PublicationScheduleProperty extends SelfNamingNotificationProperty {

    @Inject
    private PostService postService;

    public String getValue(Resource resource, Action action) {
        if (resource.getState() == State.ACCEPTED) {
            return "immediately";
        }

        String liveTimestamp = postService.getEffectiveLiveTimestamp((Post) resource).format(BoardUtils.DATETIME_FORMATTER);
        return "on or around " + liveTimestamp + ". We will send you a follow-up message when your post has gone live";
    }

}
