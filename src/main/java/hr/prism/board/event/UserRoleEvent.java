package hr.prism.board.event;

import hr.prism.board.dto.ResourceUsersDTO;
import org.springframework.context.ApplicationEvent;

public class UserRoleEvent extends ApplicationEvent {

    private Long creatorId;

    private Long resourceId;

    private ResourceUsersDTO resourceUsersDTO;

    public UserRoleEvent(Object source, Long creatorId, Long resourceId, ResourceUsersDTO resourceUsersDTO) {
        super(source);
        this.creatorId = creatorId;
        this.resourceId = resourceId;
        this.resourceUsersDTO = resourceUsersDTO;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public ResourceUsersDTO getResourceUsersDTO() {
        return resourceUsersDTO;
    }

}
