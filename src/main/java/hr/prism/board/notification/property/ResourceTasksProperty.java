package hr.prism.board.notification.property;

import hr.prism.board.domain.Resource;
import hr.prism.board.enums.ResourceTask;
import hr.prism.board.exception.BoardNotificationException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.service.NotificationService;
import hr.prism.board.service.ResourceTaskService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
@SuppressWarnings("SpringAutowiredFieldsWarningInspection")
public class ResourceTasksProperty implements NotificationProperty {

    @Inject
    private ResourceTaskService resourceTaskService;

    // TODO: add the content for each value
    public String getValue(NotificationService.NotificationRequest notificationRequest) {
        String value = StringUtils.EMPTY;
        Resource resource = notificationRequest.getResource();
        for (ResourceTask task : resourceTaskService.findByResource(resource)) {
            switch (task) {
                case CREATE_MEMBER:
                    break;
                case UPDATE_MEMBER:
                    break;
                case CREATE_INTERNAL_POST:
                    break;
                case UPDATE_INTERNAL_POST:
                    break;
                case DEPLOY_BADGE:
                    break;
            }
        }

        if (StringUtils.isEmpty(value)) {
            throw new BoardNotificationException(ExceptionCode.EMPTY_RESOURCE_TASKS, "No tasks for resource " + resource.toString());
        }

        return value;
    }

}
