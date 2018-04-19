package hr.prism.board.representation;

import hr.prism.board.enums.MemberCategory;

import java.time.LocalDate;

import static hr.prism.board.enums.UserRoleType.MEMBER;

public class MemberRepresentation extends NewUserRoleRepresentation<MemberRepresentation> {

    private String email;

    private MemberCategory memberCategory;

    private String memberProgram;

    private Integer memberYear;

    private LocalDate expiryDate;

    public MemberRepresentation() {
        super(MEMBER);
    }

    public String getEmail() {
        return email;
    }

    public MemberRepresentation setEmail(String email) {
        this.email = email;
        return this;
    }

    public MemberCategory getMemberCategory() {
        return memberCategory;
    }

    public MemberRepresentation setMemberCategory(MemberCategory memberCategory) {
        this.memberCategory = memberCategory;
        return this;
    }

    public String getMemberProgram() {
        return memberProgram;
    }

    public MemberRepresentation setMemberProgram(String memberProgram) {
        this.memberProgram = memberProgram;
        return this;
    }

    public Integer getMemberYear() {
        return memberYear;
    }

    public MemberRepresentation setMemberYear(Integer memberYear) {
        this.memberYear = memberYear;
        return this;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public MemberRepresentation setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
        return this;
    }

}
