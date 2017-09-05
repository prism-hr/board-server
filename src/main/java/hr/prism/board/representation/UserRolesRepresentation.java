package hr.prism.board.representation;

import java.util.List;

public class UserRolesRepresentation {

    private List<UserRoleRepresentation> users;

    private List<UserRoleRepresentation> memberRequests;

    private Long memberCount;

    public List<UserRoleRepresentation> getUsers() {
        return users;
    }

    public UserRolesRepresentation setUsers(List<UserRoleRepresentation> users) {
        this.users = users;
        return this;
    }

    public List<UserRoleRepresentation> getMemberRequests() {
        return memberRequests;
    }

    public UserRolesRepresentation setMemberRequests(List<UserRoleRepresentation> memberRequests) {
        this.memberRequests = memberRequests;
        return this;
    }

    public Long getMemberCount() {
        return memberCount;
    }

    public UserRolesRepresentation setMemberCount(Long memberCount) {
        this.memberCount = memberCount;
        return this;
    }

}
