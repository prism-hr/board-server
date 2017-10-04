package hr.prism.board.dto;

import hr.prism.board.enums.MemberCategory;

import javax.validation.Valid;
import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class UserRolePatchDTO {

    @Valid
    private Optional<UserDTO> user;

    private Optional<MemberCategory> memberCategory;

    private Optional<String> memberProgram;

    private Optional<Integer> memberYear;

    public Optional<UserDTO> getUser() {
        return user;
    }

    public UserRolePatchDTO setUser(Optional<UserDTO> user) {
        this.user = user;
        return this;
    }

    public Optional<MemberCategory> getMemberCategory() {
        return memberCategory;
    }

    public UserRolePatchDTO setMemberCategory(Optional<MemberCategory> memberCategory) {
        this.memberCategory = memberCategory;
        return this;
    }

    public Optional<String> getMemberProgram() {
        return memberProgram;
    }

    public UserRolePatchDTO setMemberProgram(Optional<String> memberProgram) {
        this.memberProgram = memberProgram;
        return this;
    }

    public Optional<Integer> getMemberYear() {
        return memberYear;
    }

    public UserRolePatchDTO setMemberYear(Optional<Integer> memberYear) {
        this.memberYear = memberYear;
        return this;
    }

}
