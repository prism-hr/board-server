package hr.prism.board.notification.property;

import hr.prism.board.domain.Resource;
import hr.prism.board.domain.User;
import hr.prism.board.enums.ResourceTask;
import hr.prism.board.exception.BoardNotificationException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.service.NotificationService;
import hr.prism.board.service.ResourceTaskService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;

@Component
@SuppressWarnings("SpringAutowiredFieldsWarningInspection")
public class ResourceTaskProperty implements NotificationProperty {

    private static final String CREATE_MEMBER = "Create your student list";

    private static final String UPDATE_MEMBER = "Update your student list";

    private static final String CREATE_RESEARCH_POST = "Create your current opportunities";

    private static final String UPDATE_RESEARCH_POST = "Update your current opportunities";

    private static final String DEPLOY_BADGE = "Create referral links to your homepage";

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
                    stringBuilder.append(CREATE_RESEARCH_POST);
                    break;
                case UPDATE_INTERNAL_POST:
                    stringBuilder.append(UPDATE_RESEARCH_POST);
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
