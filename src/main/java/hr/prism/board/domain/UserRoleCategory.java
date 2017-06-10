package hr.prism.board.domain;

import org.hibernate.annotations.NaturalId;

import javax.persistence.*;

@Entity
@Table(name = "user_role_category")
public class UserRoleCategory extends BoardEntity implements Comparable<UserRoleCategory> {

    @ManyToOne
    @JoinColumn(name = "user_role_id", nullable = false)
    private UserRole userRole;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @NaturalId
    @Column(name = "ordinal")
    private Integer ordinal;

    public UserRole getUserRole() {
        return userRole;
    }

    public void setUserRole(UserRole userRole) {
        this.userRole = userRole;
    }

    public String getName() {
        return name;
    }

    public UserRoleCategory setName(String name) {
        this.name = name;
        return this;
    }

    public Integer getOrdinal() {
        return ordinal;
    }

    public UserRoleCategory setOrdinal(Integer ordinal) {
        this.ordinal = ordinal;
        return this;
    }

    @Override
    public int compareTo(UserRoleCategory o) {
        return ordinal.compareTo(o.getOrdinal());
    }
}
