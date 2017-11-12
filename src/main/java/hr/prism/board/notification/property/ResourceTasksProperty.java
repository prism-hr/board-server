package hr.prism.board.notification.property;

import hr.prism.board.enums.ResourceTask;
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
    // TODO: throw an exception we can track if there are no tasks left
    public String getValue(NotificationService.NotificationRequest notificationRequest) {
        String value = StringUtils.EMPTY;
        for (ResourceTask task : resourceTaskService.findByResource(notificationRequest.getResource())) {
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
            throw new RuntimeException();
        }

        return value;
    }

}
