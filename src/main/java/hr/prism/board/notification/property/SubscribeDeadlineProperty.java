package hr.prism.board.notification.property;

import hr.prism.board.domain.Department;
import hr.prism.board.service.NotificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@SuppressWarnings("SpringAutowiredFieldsWarningInspection")
public class SubscribeDeadlineProperty implements NotificationProperty {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd LLLL yyyy");

    @Value("${department.pending.expiry.seconds}")
    private Long departmentPendingExpirySeconds;

    public String getValue(NotificationService.NotificationRequest notificationRequest) {
        Department department = (Department) notificationRequest.getResource();
        LocalDate subscribeDeadline = department.getStateChangeTimestamp().plusSeconds(departmentPendingExpirySeconds).toLocalDate();
        return subscribeDeadline.format(DATE_TIME_FORMATTER);
    }

}
