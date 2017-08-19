package hr.prism.board.representation;

import com.google.common.base.MoreObjects;
import hr.prism.board.enums.MemberCategory;
import hr.prism.board.enums.Role;
import hr.prism.board.enums.State;

import java.time.LocalDate;
import java.util.List;

public class UserRoleRepresentation {

    private UserRepresentation user;

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
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("role", role)
            .add("state", state)
            .add("expiryDate", expiryDate)
            .add("categories", categories)
            .toString();
    }

}
