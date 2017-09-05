package hr.prism.board.representation;

import java.util.List;

public class ResourceMembershipRepresentation {

    private Long memberCount;

    private List<UserRoleRepresentation> requests;

    public Long getMemberCount() {
        return memberCount;
    }

    public ResourceMembershipRepresentation setMemberCount(Long memberCount) {
        this.memberCount = memberCount;
        return this;
    }

    public List<UserRoleRepresentation> getRequests() {
        return requests;
    }

    public ResourceMembershipRepresentation setRequests(List<UserRoleRepresentation> requests) {
        this.requests = requests;
        return this;
    }

}
