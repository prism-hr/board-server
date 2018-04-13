package hr.prism.board.event;

import hr.prism.board.dto.UserRoleDTO;
import org.springframework.context.ApplicationEvent;

import java.util.List;

import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;

public class UserRoleEvent extends ApplicationEvent {

    private Long creatorId;

    private Long resourceId;

    private List<UserRoleDTO> userRoles;

    public UserRoleEvent(Object source, Long creatorId, Long resourceId, List<UserRoleDTO> userRoles) {
        super(source);
        this.creatorId = creatorId;
        this.resourceId = resourceId;
        this.userRoles = userRoles;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public List<UserRoleDTO> getUserRoles() {
        return userRoles;
    }

    @Override
    public int hashCode() {
        return reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object other) {
        return reflectionEquals(this, other);
    }

}
