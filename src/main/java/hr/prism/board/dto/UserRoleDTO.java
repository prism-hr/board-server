package hr.prism.board.dto;

import hr.prism.board.enums.MemberCategory;
import hr.prism.board.enums.Role;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class UserRoleDTO {

    private Role role;

    private LocalDate expiryDate;

    private List<MemberCategory> categories;

    public UserRoleDTO() {
    }

    public UserRoleDTO(Role role) {
        this.role = role;
    }

    public UserRoleDTO(Role role, LocalDate expiryDate, MemberCategory... categories) {
        this.role = role;
        this.expiryDate = expiryDate;
        this.categories = Arrays.asList(categories);
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
