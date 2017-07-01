package hr.prism.board.notification.property;

import hr.prism.board.domain.Resource;
import hr.prism.board.enums.Action;
import hr.prism.board.service.ResourceService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class CommentProperty extends SelfNamingNotificationProperty {

    @Inject
    private ResourceService resourceService;

    public String getValue(Resource resource, Action action) {
        return resourceService.getLatestResourceOperation(action).getComment();
    }

}
