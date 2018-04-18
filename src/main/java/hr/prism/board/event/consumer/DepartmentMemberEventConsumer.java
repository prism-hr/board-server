package hr.prism.board.event.consumer;

import hr.prism.board.dto.MemberDTO;
import hr.prism.board.event.DepartmentMemberEvent;
import hr.prism.board.exception.BoardException;
import hr.prism.board.service.DepartmentUserService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import javax.inject.Inject;

import static hr.prism.board.exception.ExceptionCode.UNPROCESSABLE_RESOURCE_USER;

@Component
public class DepartmentMemberEventConsumer {

    private final DepartmentUserService departmentUserService;

    @Inject
    public DepartmentMemberEventConsumer(DepartmentUserService departmentUserService) {
        this.departmentUserService = departmentUserService;
    }

    @Async
    @TransactionalEventListener
    public void consume(DepartmentMemberEvent departmentMemberEvent) {
        Long resourceId = departmentMemberEvent.getDepartmentId();
        for (MemberDTO member : departmentMemberEvent.getMembers()) {
            try {
                departmentUserService.createOrUpdateUserRole(resourceId, member);
            } catch (Throwable t) {
                throw new BoardException(
                    UNPROCESSABLE_RESOURCE_USER, "Unable to add member: " + member.getUser().toString(), t);
            } finally {
                departmentUserService.decrementMemberCountPending(resourceId);
            }
        }
    }

}
