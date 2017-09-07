package hr.prism.board.representation;

import java.util.List;

public class UserRolesRepresentation {

    private List<UserRoleRepresentation> users;

    private List<UserRoleRepresentation> members;

    private List<UserRoleRepresentation> memberRequests;

    public List<UserRoleRepresentation> getUsers() {
        return users;
    }

    public UserRolesRepresentation setUsers(List<UserRoleRepresentation> users) {
        this.users = users;
        return this;
    }

    public List<UserRoleRepresentation> getMembers() {
        return members;
    }

    public UserRolesRepresentation setMembers(List<UserRoleRepresentation> members) {
        this.members = members;
        return this;
    }

    public List<UserRoleRepresentation> getMemberRequests() {
        return memberRequests;
    }

    public UserRolesRepresentation setMemberRequests(List<UserRoleRepresentation> memberRequests) {
        this.memberRequests = memberRequests;
        return this;
    }

}
