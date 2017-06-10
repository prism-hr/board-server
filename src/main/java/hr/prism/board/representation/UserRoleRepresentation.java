package hr.prism.board.representation;

import hr.prism.board.domain.Role;

import java.time.LocalDate;
import java.util.List;

public class UserRoleRepresentation {

    private Role role;

    private LocalDate expiryDate;

    private List<String> categories;

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

    public List<String> getCategories() {
        return categories;
    }

    public UserRoleRepresentation setCategories(List<String> categories) {
        this.categories = categories;
        return this;
    }
}
