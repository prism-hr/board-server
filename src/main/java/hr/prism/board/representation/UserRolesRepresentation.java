package hr.prism.board.representation;

import java.util.List;

public class UserRolesRepresentation {

    private List<StaffRepresentation> staff;

    private List<MemberRepresentation> members;

    private List<MemberRepresentation> memberRequests;

    private Long memberToBeUploadedCount;

    public List<StaffRepresentation> getStaff() {
        return staff;
    }

    public UserRolesRepresentation setStaff(List<StaffRepresentation> staff) {
        this.staff = staff;
        return this;
    }

    public List<MemberRepresentation> getMembers() {
        return members;
    }

    public UserRolesRepresentation setMembers(List<MemberRepresentation> members) {
        this.members = members;
        return this;
    }

    public List<MemberRepresentation> getMemberRequests() {
        return memberRequests;
    }

    public UserRolesRepresentation setMemberRequests(List<MemberRepresentation> memberRequests) {
        this.memberRequests = memberRequests;
        return this;
    }

    public Long getMemberToBeUploadedCount() {
        return memberToBeUploadedCount;
    }

    public UserRolesRepresentation setMemberToBeUploadedCount(Long memberToBeUploadedCount) {
        this.memberToBeUploadedCount = memberToBeUploadedCount;
        return this;
    }

}
