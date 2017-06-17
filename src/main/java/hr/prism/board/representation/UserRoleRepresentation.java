package hr.prism.board.representation;

import com.google.common.base.MoreObjects;
import hr.prism.board.domain.Role;
import hr.prism.board.enums.MemberCategory;

import java.time.LocalDate;
import java.util.List;

public class UserRoleRepresentation {

    private Role role;

    private LocalDate expiryDate;

    private List<MemberCategory> categories;

    public Role getRole() {
        return role;
    }

    public UserRoleRepresentation setRole(Role role) {
        this.role = role;
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

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("role", role)
            .add("expiryDate", expiryDate)
            .add("categories", categories)
            .toString();
    }
}