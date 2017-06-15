package hr.prism.board.dto;

import hr.prism.board.domain.Role;
import hr.prism.board.enums.MemberCategory;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public class UserRoleDTO {

    @NotNull
    private Role role;

    private LocalDate expiryDate;

    private List<MemberCategory> categories;

    public UserRoleDTO(Role role) {
        this.role = role;
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

    public List<MemberCategory> getCategories() {
        return categories;
    }

    public UserRoleDTO setCategories(List<MemberCategory> categories) {
        this.categories = categories;
        return this;
    }

}
