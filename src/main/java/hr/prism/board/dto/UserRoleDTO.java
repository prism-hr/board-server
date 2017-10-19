package hr.prism.board.dto;

import hr.prism.board.enums.MemberCategory;
import hr.prism.board.enums.Role;
import org.hibernate.validator.constraints.Email;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.Objects;

public class UserRoleDTO {

    @Valid
    private UserDTO user;

    @Email
    private String email;

    private Role role;

    private MemberCategory memberCategory;

    private String memberProgram;

    private Integer memberYear;

    private LocalDate expiryDate;

    public UserRoleDTO() {
    }

    public UserRoleDTO(Role role) {
        this.role = role;
    }

    public UserRoleDTO(Role role, LocalDate expiryDate, MemberCategory memberCategory) {
        this.role = role;
        this.memberCategory = memberCategory;
        this.expiryDate = expiryDate;
    }

    public UserDTO getUser() {
        return user;
    }

    public UserRoleDTO setUser(UserDTO user) {
        this.user = user;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public UserRoleDTO setEmail(String email) {
        this.email = email;
        return this;
    }

    public Role getRole() {
        return role;
    }

    public UserRoleDTO setRole(Role role) {
        this.role = role;
        return this;
    }

    public MemberCategory getMemberCategory() {
        return memberCategory;
    }

    public UserRoleDTO setMemberCategory(MemberCategory memberCategory) {
        this.memberCategory = memberCategory;
        return this;
    }

    public String getMemberProgram() {
        return memberProgram;
    }

    public UserRoleDTO setMemberProgram(String memberProgram) {
        this.memberProgram = memberProgram;
        return this;
    }

    public Integer getMemberYear() {
        return memberYear;
    }

    public UserRoleDTO setMemberYear(Integer memberYear) {
        this.memberYear = memberYear;
        return this;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public UserRoleDTO setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(role);
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || object.getClass() != getClass()) {
            return false;
        }

        return Objects.equals(role, ((UserRoleDTO) object).getRole());
    }

}
