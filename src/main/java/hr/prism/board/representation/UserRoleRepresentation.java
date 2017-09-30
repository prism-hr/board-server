package hr.prism.board.representation;

import hr.prism.board.enums.MemberCategory;
import hr.prism.board.enums.Role;
import hr.prism.board.enums.State;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public class UserRoleRepresentation {

    private UserRepresentation user;

    private String email;

    private Role role;

    private State state;

    private LocalDate expiryDate;

    private List<MemberCategory> categories;

    private boolean viewed;

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

    public List<MemberCategory> getCategories() {
        return categories;
    }

    public UserRoleRepresentation setCategories(List<MemberCategory> categories) {
        this.categories = categories;
        return this;
    }

    public boolean isViewed() {
        return viewed;
    }

    public UserRoleRepresentation setViewed(boolean viewed) {
        this.viewed = viewed;
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, role, state);
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || object.getClass() != getClass()) {
            return false;
        }

        UserRoleRepresentation that = (UserRoleRepresentation) object;
        return user.equals(that.getUser()) && role == that.getRole() && state == that.getState();
    }

}
