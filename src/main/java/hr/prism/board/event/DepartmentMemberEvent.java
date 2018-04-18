package hr.prism.board.event;

import hr.prism.board.dto.MemberDTO;
import org.springframework.context.ApplicationEvent;

import java.util.List;

import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;

public class DepartmentMemberEvent extends ApplicationEvent {

    private Long departmentId;

    private List<MemberDTO> members;

    public DepartmentMemberEvent(Object source, Long departmentId, List<MemberDTO> members) {
        super(source);
        this.departmentId = departmentId;
        this.members = members;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public List<MemberDTO> getMembers() {
        return members;
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
