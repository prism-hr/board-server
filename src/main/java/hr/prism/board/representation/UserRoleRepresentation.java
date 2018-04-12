package hr.prism.board.representation;

import hr.prism.board.enums.MemberCategory;
import hr.prism.board.enums.Role;
import hr.prism.board.enums.State;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.util.Objects.hash;

public class UserRoleRepresentation {

    private UserRepresentation user;

    private String email;

    private Role role;

    private MemberCategory memberCategory;

    private String memberProgram;

    private Integer memberYear;

    private State state;

    private LocalDate expiryDate;

    private boolean viewed;

    private LocalDateTime createdTimestamp;

    private LocalDateTime updatedTimestamp;

    public UserRepresentation getUser() {
        return user;
    }

    public UserRoleRepresentation setUser(UserRepresentation user) {
        this.user = user;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public UserRoleRepresentation setEmail(String email) {
        this.email = email;
        return this;
    }

    public Role getRole() {
        return role;
    }

    public UserRoleRepresentation setRole(Role role) {
        this.role = role;
        return this;
    }

    public MemberCategory getMemberCategory() {
        return memberCategory;
    }

    public UserRoleRepresentation setMemberCategory(MemberCategory memberCategory) {
        this.memberCategory = memberCategory;
        return this;
    }

    public String getMemberProgram() {
        return memberProgram;
    }

    public UserRoleRepresentation setMemberProgram(String memberProgram) {
        this.memberProgram = memberProgram;
        return this;
    }

    public Integer getMemberYear() {
        return memberYear;
    }

    public UserRoleRepresentation setMemberYear(Integer memberYear) {
        this.memberYear = memberYear;
        return this;
    }

    public State getState() {
        return state;
    }

    public UserRoleRepresentation setState(State state) {
        this.state = state;
        return this;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public UserRoleRepresentation setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
        return this;
    }

    public boolean isViewed() {
        return viewed;
    }

    public UserRoleRepresentation setViewed(boolean viewed) {
        this.viewed = viewed;
        return this;
    }

    public LocalDateTime getCreatedTimestamp() {
        return createdTimestamp;
    }

    public UserRoleRepresentation setCreatedTimestamp(LocalDateTime createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
        return this;
    }

    public LocalDateTime getUpdatedTimestamp() {
        return updatedTimestamp;
    }

    public UserRoleRepresentation setUpdatedTimestamp(LocalDateTime updatedTimestamp) {
        this.updatedTimestamp = updatedTimestamp;
        return this;
    }

    @Override
    public int hashCode() {
        return hash(user, role, state);
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || object.getClass() != getClass()) {
            return false;
        }

        UserRoleRepresentation that = (UserRoleRepresentation) object;
        return user.equals(that.getUser())
            && role == that.getRole()
            && state == that.getState();
    }

}
