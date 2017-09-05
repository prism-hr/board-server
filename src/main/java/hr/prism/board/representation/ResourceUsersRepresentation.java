package hr.prism.board.representation;

import java.util.List;

public class ResourceUsersRepresentation {

    private List<ResourceUserRepresentation> users;

    private List<UserRoleRepresentation> memberRequests;

    private Long memberCount;

    public List<ResourceUserRepresentation> getUsers() {
        return users;
    }

    public ResourceUsersRepresentation setUsers(List<ResourceUserRepresentation> users) {
        this.users = users;
        return this;
    }

    public List<UserRoleRepresentation> getMemberRequests() {
        return memberRequests;
    }

    public ResourceUsersRepresentation setMemberRequests(List<UserRoleRepresentation> memberRequests) {
        this.memberRequests = memberRequests;
        return this;
    }

    public Long getMemberCount() {
        return memberCount;
    }

    public ResourceUsersRepresentation setMemberCount(Long memberCount) {
        this.memberCount = memberCount;
        return this;
    }

}
