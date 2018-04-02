package hr.prism.board.notification.property;

import hr.prism.board.domain.Resource;
import hr.prism.board.enums.ResourceTask;
import hr.prism.board.exception.BoardNotificationException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.service.NotificationService.NotificationRequest;
import hr.prism.board.service.ResourceTaskService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;

@Component
@SuppressWarnings("SpringAutowiredFieldsWarningInspection")
public class ResourceTaskProperty implements NotificationProperty {

    private static final String CREATE_MEMBER = "Ready to get started - visit the user management area to build your student list.";

    private static final String UPDATE_MEMBER = "New students arriving - visit the user management area to update your student list.";

    private static final String CREATE_POST = "Got something to share - create some posts and start sending notifications.";

    private static final String DEPLOY_BADGE = "Time to tell the world - go to the badges section to learn about promoting your board to your website.";

    @Inject
    private ResourceTaskService resourceTaskService;

    public String getValue(NotificationRequest notificationRequest) {
        Resource resource = notificationRequest.getResource();
        List<ResourceTask> tasks = resourceTaskService.getTasks(resource);
        if (tasks.isEmpty()) {
            throw new BoardNotificationException(ExceptionCode.EMPTY_RESOURCE_TASKS, "No tasks for resource " + resource.toString());
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<ul>");
        for (ResourceTask task : tasks) {
            stringBuilder.append("<li>");
            switch (task) {
                case CREATE_MEMBER:
                    stringBuilder.append(CREATE_MEMBER);
                    break;
                case UPDATE_MEMBER:
                    stringBuilder.append(UPDATE_MEMBER);
                    break;
                case CREATE_POST:
                    stringBuilder.append(CREATE_POST);
                    break;
                case DEPLOY_BADGE:
                    stringBuilder.append(DEPLOY_BADGE);
                    break;
            }

            stringBuilder.append("</li>");
        }

        stringBuilder.append("</ul>");
        return stringBuilder.toString();
    }

}
