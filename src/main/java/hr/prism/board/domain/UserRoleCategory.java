package hr.prism.board.domain;

import hr.prism.board.enums.MemberCategory;
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;

@Entity
@Table(name = "user_role_category")
public class UserRoleCategory extends BoardEntity implements Comparable<UserRoleCategory> {

    @ManyToOne
    @JoinColumn(name = "user_role_id", nullable = false)
    private UserRole userRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false)
    private MemberCategory name;

    @NaturalId
    @Column(name = "ordinal")
    private Integer ordinal;

    public UserRole getUserRole() {
        return userRole;
    }

    public void setUserRole(UserRole userRole) {
        this.userRole = userRole;
    }

    public MemberCategory getName() {
        return name;
    }

    public UserRoleCategory setName(MemberCategory name) {
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
    public int compareTo(UserRoleCategory object) {
        return ordinal.compareTo(object.getOrdinal());
    }

}
