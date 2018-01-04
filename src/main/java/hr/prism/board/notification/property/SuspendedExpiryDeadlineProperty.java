package hr.prism.board.notification.property;

import hr.prism.board.domain.Department;
import hr.prism.board.service.NotificationService;
import hr.prism.board.utils.BoardUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@SuppressWarnings("SpringAutowiredFieldsWarningInspection")
public class SuspendedExpiryDeadlineProperty implements NotificationProperty {

    @Value("${department.suspended.expiry.seconds}")
    private Long departmentSuspendedExpirySeconds;

    public String getValue(NotificationService.NotificationRequest notificationRequest) {
        Department department = (Department) notificationRequest.getResource();
        LocalDate deadline = department.getStateChangeTimestamp().plusSeconds(departmentSuspendedExpirySeconds).toLocalDate();
        return deadline.format(BoardUtils.DATETIME_FORMATTER);
    }

}
