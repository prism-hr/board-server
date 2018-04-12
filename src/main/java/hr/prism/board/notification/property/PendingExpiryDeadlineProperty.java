package hr.prism.board.notification.property;

import hr.prism.board.domain.Department;
import hr.prism.board.service.NotificationService.NotificationRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.LocalDate;

import static hr.prism.board.utils.BoardUtils.DATETIME_FORMATTER;

@Component
public class PendingExpiryDeadlineProperty implements NotificationProperty {

    private final Long departmentPendingExpirySeconds;

    @Inject
    public PendingExpiryDeadlineProperty(
        @Value("${department.pending.expiry.seconds}") Long departmentPendingExpirySeconds) {
        this.departmentPendingExpirySeconds = departmentPendingExpirySeconds;
    }

    public String getValue(NotificationRequest notificationRequest) {
        Department department = (Department) notificationRequest.getResource();
        LocalDate deadline = department.getStateChangeTimestamp()
            .plusSeconds(departmentPendingExpirySeconds).toLocalDate();
        return deadline.format(DATETIME_FORMATTER);
    }

}
