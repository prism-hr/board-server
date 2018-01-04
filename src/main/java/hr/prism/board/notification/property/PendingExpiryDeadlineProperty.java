package hr.prism.board.notification.property;

import hr.prism.board.domain.Department;
import hr.prism.board.service.NotificationService;
import hr.prism.board.utils.BoardUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@SuppressWarnings("SpringAutowiredFieldsWarningInspection")
public class PendingExpiryDeadlineProperty implements NotificationProperty {

    @Value("${department.pending.expiry.seconds}")
    private Long departmentPendingExpirySeconds;

    public String getValue(NotificationService.NotificationRequest notificationRequest) {
        Department department = (Department) notificationRequest.getResource();
        LocalDate deadline = department.getStateChangeTimestamp().plusSeconds(departmentPendingExpirySeconds).toLocalDate();
        return deadline.format(BoardUtils.DATETIME_FORMATTER);
    }

}
