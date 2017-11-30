package hr.prism.board.notification.property;

import org.springframework.stereotype.Component;

import java.util.List;

import javax.inject.Inject;

import hr.prism.board.domain.Resource;
import hr.prism.board.domain.User;
import hr.prism.board.enums.ResourceTask;
import hr.prism.board.exception.BoardNotificationException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.service.NotificationService;
import hr.prism.board.service.ResourceTaskService;

@Component
@SuppressWarnings("SpringAutowiredFieldsWarningInspection")
public class ResourceTaskProperty implements NotificationProperty {

    private static final String CREATE_MEMBER = "Add some members - visit the user management section to build your student list and start sending notifications.";

    private static final String UPDATE_MEMBER = "Add some new members - visit the user management to add this year's new students to your student list.";

    private static final String CREATE_POST = "Got an opportunity to share - go to the new post form to start adding content.";

    private static final String DEPLOY_BADGE = "";

    @Inject
    private ResourceTaskService resourceTaskService;

    public String getValue(NotificationService.NotificationRequest notificationRequest) {
        User user = notificationRequest.getRecipient();
        Resource resource = notificationRequest.getResource();
        List<ResourceTask> tasks = resourceTaskService.findByResource(resource, user);
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
