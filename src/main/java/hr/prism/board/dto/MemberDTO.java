package hr.prism.board.dto;

import hr.prism.board.enums.MemberCategory;
import org.hibernate.validator.constraints.Email;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

public class MemberDTO {

    @Valid
    @NotNull
    private UserDTO user;

    @Email
    private String email;

    private MemberCategory memberCategory;

    private String memberProgram;

    private Integer memberYear;

    private LocalDate expiryDate;

    public UserDTO getUser() {
        return user;
    }

    public MemberDTO setUser(UserDTO user) {
        this.user = user;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public MemberDTO setEmail(String email) {
        this.email = email;
        return this;
    }

    public MemberCategory getMemberCategory() {
        return memberCategory;
    }

    public MemberDTO setMemberCategory(MemberCategory memberCategory) {
        this.memberCategory = memberCategory;
        return this;
    }

    public String getMemberProgram() {
        return memberProgram;
    }

    public MemberDTO setMemberProgram(String memberProgram) {
        this.memberProgram = memberProgram;
        return this;
    }

    public Integer getMemberYear() {
        return memberYear;
    }

    public MemberDTO setMemberYear(Integer memberYear) {
        this.memberYear = memberYear;
        return this;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public MemberDTO setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
        return this;
    }

}
