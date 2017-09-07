package hr.prism.board.representation;

import java.util.ArrayList;
import java.util.List;

public class UserRolesRepresentation {

    private List<UserRoleRepresentation> users = new ArrayList<>();

    private List<UserRoleRepresentation> members = new ArrayList<>();

    private List<UserRoleRepresentation> memberRequests = new ArrayList<>();

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
