package hr.prism.board.dto;

import hr.prism.board.domain.Role;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class UserRoleDTO {

    @NotNull
    private Role role;

    private LocalDate expiryDate;

    private List<String> categories;

    public UserRoleDTO(Role role) {
        this.role = role;
    }

    public UserRoleDTO(Role role, LocalDate expiryDate, String... categories) {
        this.role = role;
        this.expiryDate = expiryDate;
        this.categories = Arrays.asList(categories);
    }

    public UserRoleDTO() {
    }

    public Role getRole() {
        return role;
    }

    public UserRoleDTO setRole(Role role) {
        this.role = role;
        return this;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public UserRoleDTO setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
        return this;
    }

    public List<String> getCategories() {
        return categories;
    }

    public UserRoleDTO setCategories(List<String> categories) {
        this.categories = categories;
        return this;
    }
}
