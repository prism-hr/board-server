package hr.prism.board.value;

import hr.prism.board.domain.UserRole;

import java.util.List;

public class UserRoles {

    private List<UserRole> staff;

    private List<UserRole> members;

    private List<UserRole> memberRequests;

    private Long memberToBeUploadedCount;

    public List<UserRole> getStaff() {
        return staff;
    }

    public UserRoles setStaff(List<UserRole> staff) {
        this.staff = staff;
        return this;
    }

    public List<UserRole> getMembers() {
        return members;
    }

    public UserRoles setMembers(List<UserRole> members) {
        this.members = members;
        return this;
    }

    public List<UserRole> getMemberRequests() {
        return memberRequests;
    }

    public UserRoles setMemberRequests(List<UserRole> memberRequests) {
        this.memberRequests = memberRequests;
        return this;
    }

    public Long getMemberToBeUploadedCount() {
        return memberToBeUploadedCount;
    }

    public UserRoles setMemberToBeUploadedCount(Long memberToBeUploadedCount) {
        this.memberToBeUploadedCount = memberToBeUploadedCount;
        return this;
    }

}
