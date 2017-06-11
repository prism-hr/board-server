package hr.prism.board.event;

import hr.prism.board.dto.ResourceUsersDTO;
import org.springframework.context.ApplicationEvent;

public class UserRoleEvent extends ApplicationEvent {

    private Long resourceId;

    private ResourceUsersDTO resourceUsersDTO;

    public UserRoleEvent(Object source, Long resourceId, ResourceUsersDTO resourceUsersDTO) {
        super(source);
        this.resourceId = resourceId;
        this.resourceUsersDTO = resourceUsersDTO;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public ResourceUsersDTO getResourceUsersDTO() {
        return resourceUsersDTO;
    }

}
